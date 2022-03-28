package com.usian.config;

import com.rabbitmq.client.Channel;
import com.usian.pojo.DeDuplication;
import com.usian.pojo.LocalMessage;
import com.usian.service.DeDuplicationService;
import com.usian.service.ItemService;
import com.usian.utils.JsonUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Loser
 * @date 2021年12月04日 14:12
 */
@Component
public class ItemMQListener {

    @Autowired
    private ItemService itemService;
    @Autowired
    private DeDuplicationService deDuplicationService;

    /**
     * 设置rabbitMQ
     * @param orderId
     * @param channel
     * @param message
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "order_queue"),
            exchange = @Exchange(value="order_exchange",type = ExchangeTypes.TOPIC),
            key = {"*.*"}
    ))
    public void Listener(String orderId, Channel channel, Message message){

        try {
            LocalMessage localMessage = JsonUtils.jsonToPojo(orderId, LocalMessage.class);

            //接收消息
            System.out.println("接收到了消息: " + orderId);
            //3. 再次收到消息查询消息去重表
            DeDuplication deDuplication = deDuplicationService.selectDeDuplicationByTxNo(localMessage.getTxNo());
            if (deDuplication == null){
                //1. 扣库存
                itemService.updateTbItemByOrderId(localMessage.getTxNo());
                //2. 记录消息到去重表
                deDuplicationService.addDeDuplication(localMessage.getTxNo());
            }else {
                System.out.println("=================幂等生效：事务"+deDuplication.getTxNo()
                        +" 已然成功执行================");
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
