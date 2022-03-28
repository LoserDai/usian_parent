package com.usian.service;

import com.usian.config.RedisClient;
import com.usian.mapper.*;
import com.usian.mq.MQSender;
import com.usian.pojo.*;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Loser
 * @date 2021年12月03日 15:23
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {
    @Autowired
    private TbOrderMapper tbOrderMapper;
    @Autowired
    private TbOrderShippingMapper tbOrderShippingMapper;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Value("${ORDER_ID_KEY}")
    private String ORDER_ID_KEY;

    @Value("${ORDER_ITEM_ID_KEY}")
    private String ORDER_ITEM_ID_KEY;

    @Value("${ORDER_ID_BEGIN}")
    private Long ORDER_ID_BEGIN;

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private LocalMessageMapper localMessageMapper;
    @Autowired
    private MQSender mqSender;

    @Override
    public Long insertOrder(OrderInfo orderInfo) {
        String orderItem = orderInfo.getOrderItem();
        List<TbOrderItem> tbOrderItemList = JsonUtils.jsonToList(orderItem, TbOrderItem.class);
        //获取订单
        TbOrder tbOrder = orderInfo.getTbOrder();
        TbOrderShipping tbOrderShipping = orderInfo.getTbOrderShipping();

        //保存订单表: orderId、status、createTime、updateTime
        if(!redisClient.exists(ORDER_ID_KEY)){
            redisClient.set(ORDER_ID_KEY,ORDER_ID_BEGIN);
        }
        //自增长订单Id: orderId
        Long orderId = redisClient.incr(ORDER_ID_KEY, 1L);
        tbOrder.setOrderId(orderId.toString());
        tbOrder.setStatus(1);
        tbOrder.setCreateTime(new Date());
        tbOrder.setUpdateTime(new Date());
        tbOrderMapper.insertSelective(tbOrder);
        //保存订单详情表
        if(!redisClient.exists(ORDER_ITEM_ID_KEY)){
            redisClient.set(ORDER_ITEM_ID_KEY,0);
        }
        for (TbOrderItem tbOrderItem : tbOrderItemList) {
            Long orderItemId = redisClient.incr(ORDER_ITEM_ID_KEY, 1);
            tbOrderItem.setOrderId(orderId.toString());
            tbOrderItem.setId(orderItemId.toString());
            tbOrderItemMapper.insertSelective(tbOrderItem);
        }
        //保存订单物流表
        tbOrderShipping.setOrderId(orderId.toString());
        tbOrderShipping.setCreated(new Date());
        tbOrderShipping.setUpdated(new Date());
        tbOrderShippingMapper.insertSelective(tbOrderShipping);

        //======2.把消息存到local_Message
        LocalMessage localMessage = new LocalMessage();
        localMessage.setTxNo(UUID.randomUUID().toString());
        localMessage.setOrderNo(orderId.toString());
        localMessage.setState(0);
        localMessageMapper.insertSelective(localMessage);

        //发布消息到MQ
        amqpTemplate.convertAndSend("order_exchange","order.add",orderId);
        //3. 发送消息
        mqSender.sendMsg(localMessage);
        return orderId;
    }

    @Override
    public void closeTimeOutOrder() {
        //1、查询超时订单： 线上付款 状态未付款 创建时间<=now()-2
        TbOrderExample tbOrderExample = new TbOrderExample();
        TbOrderExample.Criteria criteria = tbOrderExample.createCriteria();
        criteria.andStatusEqualTo(1);
        criteria.andPaymentTypeEqualTo(1);
        Calendar calendar = Calendar.getInstance();
        //获取超时时间: 比创建订单的时间晚两天
        calendar.add(Calendar.DAY_OF_MONTH,-2);
        criteria.andCreateTimeLessThan(calendar.getTime());
        //查询出超时订单
        List<TbOrder> tbOrderList = tbOrderMapper.selectByExample(tbOrderExample);
        //2、关闭超时订单： 状态已关闭 关闭时间 修改时间.
        for (TbOrder tbOrder : tbOrderList) {
            tbOrder.setStatus(6);
            tbOrder.setUpdateTime(new Date());
            tbOrder.setCloseTime(new Date());
            tbOrderMapper.updateByPrimaryKey(tbOrder);
            //3、把库存加回去
            TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
            TbOrderItemExample.Criteria criteria1 = tbOrderItemExample.createCriteria();
            criteria1.andOrderIdEqualTo(tbOrder.getOrderId());
            //查询出订单中的所有商品
            List<TbOrderItem> tbOrderItemList = tbOrderItemMapper.selectByExample(tbOrderItemExample);
            for (TbOrderItem tbOrderItem : tbOrderItemList) {
                TbItem tbItem = tbItemMapper.selectByPrimaryKey(Long.valueOf(tbOrderItem.getItemId()));
                //库存回滚
                tbItem.setNum(tbItem.getNum() + tbOrderItem.getNum());
                tbItemMapper.updateByPrimaryKey(tbItem);
            }
        }
    }

    @Override
    public void scanAndSendOfLocalMessage() {
        LocalMessageExample localMessageExample = new LocalMessageExample();
        LocalMessageExample.Criteria criteria = localMessageExample.createCriteria();
        criteria.andStateEqualTo(0);
        List<LocalMessage> localMessageList = localMessageMapper.selectByExample(localMessageExample);
        for (LocalMessage localMessage : localMessageList) {
            //发送消息
            mqSender.sendMsg(localMessage);
        }
    }
}
