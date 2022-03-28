package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.config.RedisClient;
import com.usian.mapper.TbItemParamItemMapper;
import com.usian.mapper.TbItemParamMapper;
import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamExample;
import com.usian.pojo.TbItemParamItem;
import com.usian.pojo.TbItemParamItemExample;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Loser
 * @date 2021年11月18日 19:25
 */
@Service
@Transactional
public class ItemParamServiceImpl implements ItemParamService {
    @Autowired
    private TbItemParamMapper tbItemParamMapper;
    @Autowired
    private TbItemParamItemMapper tbItemParamItemMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${ITEM_INFO}")
    private String ITEM_INFO;
    @Value("${PARAM}")
    private String PARAM;
    @Value("${ITEM_INFO_EXPIRE}")
    private Integer ITEM_INFO_EXPIRE;
    @Value("SETNX_PARAM_LOCK_KEY")
    private String SETNX_PARAM_LOCK_KEY;

    @Override
    public TbItemParam selectItemParamByItemCatId(Integer itemCatId) {
        TbItemParamExample itemParamExample = new TbItemParamExample();
        TbItemParamExample.Criteria criteria = itemParamExample.createCriteria();
        criteria.andItemCatIdEqualTo(Long.valueOf(itemCatId));
        List<TbItemParam> list = tbItemParamMapper.selectByExampleWithBLOBs(itemParamExample);
        if (list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    /**
     * 添加规格模板
     * @param tbItemParam
     * @return
     */
    @Override
    public Integer insertItemParam(TbItemParam tbItemParam) {
        tbItemParam.setCreated(new Date());
        tbItemParam.setUpdated(new Date());
        int count = tbItemParamMapper.insertSelective(tbItemParam);
        return count;
    }

    /**
     * 删除规格模板
     * @param id
     * @return
     */
    @Override
    public Integer deleteItemParamById(Integer id) {
        int count = tbItemParamMapper.deleteByPrimaryKey(Long.valueOf(id));
        return count;
    }

    /**
     * 规格分页查询
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult selectItemParamAll(Integer page, Integer rows) {
        PageHelper.startPage(page,rows);
        List<TbItemParam> tbItemParams = tbItemParamMapper.selectByExampleWithBLOBs(null);
        PageInfo<TbItemParam> pageInfo = new PageInfo<>(tbItemParams);

        PageResult pageResult = new PageResult();
        pageResult.setResult(tbItemParams); //返回结果集合
        pageResult.setTotalPage((long) pageInfo.getPages()); //总页数
        pageResult.setPageIndex(pageInfo.getPageNum()); //当前页

        return pageResult;
    }

    @Override
    public TbItemParamItem selectTbItemParamItemByItemId(Long itemId) {
        //查询redis中是否有数据,有就直接返回,没有就添加再返回
        TbItemParamItem tbItemParamItem = (TbItemParamItem) redisClient.get(ITEM_INFO + ":" + itemId + ":" + PARAM);
        if (tbItemParamItem != null) {
            return tbItemParamItem;
        }
        //添加分布式锁,设置失效时间
        if (redisClient.setnx(SETNX_PARAM_LOCK_KEY + ":" + itemId, itemId, 30L)) {
            try{
                TbItemParamItemExample tbItemParamItemExample = new TbItemParamItemExample();
                TbItemParamItemExample.Criteria criteria = tbItemParamItemExample.createCriteria();
                criteria.andItemIdEqualTo(itemId);
                List<TbItemParamItem> list = tbItemParamItemMapper.selectByExampleWithBLOBs(tbItemParamItemExample);
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }
                if (tbItemParamItem == null) {
                    tbItemParamItem = new TbItemParamItem();
                    redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM, tbItemParamItem);
                    //设置有效期限
                    redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM, 30);
                    return tbItemParamItem;
                }
                //把数据存储到缓存
                redisClient.set(ITEM_INFO + ":" + itemId + ":" + PARAM, list.get(0));
                //设置有效期限
                redisClient.expire(ITEM_INFO + ":" + itemId + ":" + PARAM, ITEM_INFO_EXPIRE);
                return list.get(0);
            }finally {
                //释放锁,以防死锁
                redisClient.del(SETNX_PARAM_LOCK_KEY + ":" + itemId);
            }
        }else {
            //回调
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return selectTbItemParamItemByItemId(itemId);
        }
    }
}
