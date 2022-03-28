package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.config.RedisClient;
import com.usian.mapper.*;
import com.usian.pojo.*;
import com.usian.utils.IDUtils;
import com.usian.utils.PageResult;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Loser
 * @date 2021年11月18日 11:44
 */
@Service
@Transactional
public class ItemServiceImpl implements ItemService {
    @Autowired
    private TbItemMapper tbItemMapper;
    @Autowired
    private TbItemDescMapper tbItemDescMapper;
    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;
    @Autowired
    private TbItemCatMapper tbItemCatMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private RedisClient redisClient;
    @Value("${ITEM_INFO}")
    private String ITEM_INFO;
    @Value("${BASE}")
    private String BASE;
    @Value("${DESC}")
    private String DESC;
    @Value("${PARAM}")
    private String PARAM;
    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;
    @Value("SETNX_BASC_LOCK_KEY")
    private String SETNX_BASC_LOCK_KEY;
    @Value("SETNX_DESC_LOCK_KEY")
    private String SETNX_DESC_LOCK_KEY;
    @Autowired
    private TbOrderItemMapper tbOrderItemMapper;

    @Override
    public TbItem selectItemInfo(Long itemId){
        //首先查询redis中有没有,有就直接返回,没有就添加再返回
        TbItem tbItem = (TbItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + BASE);
        if (tbItem != null){
            return tbItem;
        }
        //添加分布式锁,设置失效时间
        if (redisClient.setnx(SETNX_BASC_LOCK_KEY + ":" + itemId,itemId,30L)){
            try {
                tbItem = tbItemMapper.selectByPrimaryKey(itemId);
                //解决缓存穿透:返回一个空对象
                if (tbItem == null){
                    tbItem = new TbItem();
                    redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,tbItem);
                    //设置缓存的有效时间
                    redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,30);
                    return tbItem;
                }
                //如果没有就添加到redis
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + BASE,tbItem);
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + BASE,ITEM_INFO_EXPIRE);
                return tbItem;
            }finally {
                //释放锁,以防死锁
                redisClient.del(SETNX_BASC_LOCK_KEY + ":" + itemId);
            }
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemInfo(itemId);
        }
    }

    /**
     * 分页查询
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectTbItemAllByPage(Integer page,Integer rows){
        PageHelper.startPage(page, rows);
        //根据条件查询商品信息: status不为 3,已修改时间为查询条件
        TbItemExample tbItemExample = new TbItemExample();
        TbItemExample.Criteria criteria = tbItemExample.createCriteria();
        criteria.andStatusNotEqualTo((byte)3);
        tbItemExample.setOrderByClause("updated DESC");
        //根据 tbItemExample 这个条件查询商品信息
        List<TbItem> tbItemList = tbItemMapper.selectByExample(tbItemExample);

        for (TbItem tbItem : tbItemList) {
            tbItem.setPrice(tbItem.getPrice() / 100);
        }

        PageInfo<TbItem> tbItemPageInfo = new PageInfo<>(tbItemList);
       //返回一个Result
        PageResult pageResult = new PageResult();
        pageResult.setResult(tbItemList);
        pageResult.setTotalPage(Long.valueOf(tbItemPageInfo.getPages()));
        pageResult.setPageIndex(tbItemPageInfo.getPageNum());

        return pageResult;
    }

    /**
     * 添加商品
     * @param tbItem
     * @return
     */
    @Override
    public Integer insertTbItem(TbItem tbItem,String desc,String itemParams) {
        //添加tbItem参数
        long ItemId = IDUtils.genItemId();
        Date date = new Date();
        tbItem.setId(ItemId);
        tbItem.setCreated(date);
        tbItem.setPrice(tbItem.getPrice() * 100);
        tbItem.setUpdated(date);
        Integer count1 = tbItemMapper.insertSelective(tbItem);

        //添加desc的参数
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(ItemId);
        tbItemDesc.setCreated(date);
        tbItemDesc.setUpdated(date);
        tbItemDesc.setItemDesc(desc);
        Integer count2 = tbItemDescMapper.insertSelective(tbItemDesc);

        //添加itemParams的参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(ItemId);
        tbItemParamItem.setCreated(date);
        tbItemParamItem.setUpdated(date);
        tbItemParamItem.setParamData(itemParams);
        Integer count3 = tbItemParamItemMapper.insertSelective(tbItemParamItem);

        Integer count = count1 + count2 + count3;

        //发送消息
        amqpTemplate.convertAndSend("item_exchange","item.add",ItemId);
        return count;
    }

    /**
     * 删除商品:修改商品的状态
     * @param itemId
     * @return
     */
    @Override
    public Integer deleteItemById(Long itemId) {
        TbItem tbItem = new TbItem();
        tbItem.setId(itemId);
        tbItem.setStatus((byte)3);
        //修改的时候删除缓存中的数据
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + BASE);
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + DESC);
        redisClient.del(ITEM_INFO + ":" + itemId + ":" + PARAM);
        return tbItemMapper.updateByPrimaryKeySelective(tbItem);
    }

    /**
     * 回显商品的信息
     * @param itemId
     * @return
     */
    @Override
    public Map<String, Object> preUpdateItem(Long itemId) {

        Map<String, Object> map = new HashMap<>();
        //显示商品信息
        TbItem item = tbItemMapper.selectByPrimaryKey(itemId);
        map.put("item",item);

        //显示商品类目
        TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(item.getCid());
        map.put("itemCat", itemCat.getName());

        //显示商品描述
        TbItemDesc itemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
        map.put("itemDesc",itemDesc.getItemDesc());

        //显示商品规格
        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria1 = tbItemParamItemExample.createCriteria();
        criteria1.andItemIdEqualTo(itemId);
        List<TbItemParamItem> list = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
        if (list.size() > 0 && list != null){
            map.put("list",list.get(0).getParamData());
        }
        return map;
    }

    /**
     * 修改商品的信息
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @Override
    public Integer updateTbItem(TbItem tbItem, String desc, String itemParams) {

        //修改标题,价格,数量,图片,时间
        Date date = new Date();
        Long id = tbItem.getId();
        tbItem.setSellPoint(tbItem.getSellPoint());
        tbItem.setPrice(tbItem.getPrice() * 100);
        tbItem.setImage(tbItem.getImage());
        tbItem.setNum(tbItem.getNum());
        tbItem.setUpdated(date);
        int count1 = tbItemMapper.updateByPrimaryKeySelective(tbItem);

        //修改规格参数
        TbItemParamItem tbItemParamItem = new TbItemParamItem();
        tbItemParamItem.setItemId(tbItem.getId());
        tbItemParamItem.setUpdated(date);
        tbItemParamItem.setParamData(itemParams);

        TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
        TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
        criteria.andItemIdEqualTo(tbItem.getId());
        int count2 = tbItemParamItemMapper.updateByExampleSelective(tbItemParamItem, tbItemParamItemExample);

        //修改商品描述
        TbItemDesc tbItemDesc = new TbItemDesc();
        tbItemDesc.setItemId(tbItem.getId());
        tbItemDesc.setItemDesc(desc);
        tbItemDesc.setUpdated(date);
        int count3 = tbItemDescMapper.updateByPrimaryKeySelective(tbItemDesc);

        int count = count1 + count2 +count3;
        //修改的时候删除缓存中的数据
        redisClient.del(ITEM_INFO + ":" + id + ":" + BASE);
        redisClient.del(ITEM_INFO + ":" + id + ":" + DESC);
        redisClient.del(ITEM_INFO + ":" + id + ":" + PARAM);

        return count;
    }

    @Override
    public TbItemDesc selectItemDescByItemId(Long itemId) {
        //首先查询redis中有没有,有就直接返回,没有就添加再返回
        TbItemDesc tbItemDesc = (TbItemDesc) redisClient.get(ITEM_INFO + ":" + itemId + ":" + DESC);
        if (tbItemDesc != null){
            return tbItemDesc;
        }
        //添加分布式锁,设置失效时间
        if (redisClient.setnx(SETNX_DESC_LOCK_KEY + ":" + itemId,itemId,30L)){
            try{
                //添加到redis并设置过期时间
                tbItemDesc = tbItemDescMapper.selectByPrimaryKey(itemId);
                if(tbItemDesc == null){
                    tbItemDesc = new TbItemDesc();
                    redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC, tbItemDesc);
                    redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,30);
                    return tbItemDesc;
                }
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + DESC, tbItemDesc);
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + DESC,ITEM_INFO_EXPIRE);
                return tbItemDesc;
            }finally {
                //释放锁,以防死锁
                redisClient.del(SETNX_DESC_LOCK_KEY + ":" + itemId);
            }
        }else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectItemDescByItemId(itemId);
        }
    }

    @Override
    public void updateTbItemByOrderId(String orderId) {
        //1、查询tb_order_item--->List<tbOrderItem>
        TbOrderItemExample tbOrderItemExample = new TbOrderItemExample();
        TbOrderItemExample.Criteria criteria = tbOrderItemExample.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<TbOrderItem> tbOrderItemList = tbOrderItemMapper.selectByExample(tbOrderItemExample);

        //2、遍历List<tbOrderItem>，且修改库存
        for (TbOrderItem tbOrderItem : tbOrderItemList) {
            TbItem tbItem = tbItemMapper.selectByPrimaryKey(Long.valueOf(tbOrderItem.getItemId()));
            tbItem.setNum(tbItem.getNum() - tbOrderItem.getNum());
            tbItem.setUpdated(new Date());
            tbItemMapper.updateByPrimaryKey(tbItem);
        }
    }
}
