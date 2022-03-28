package com.usian.service;

import com.netflix.discovery.converters.Auto;
import com.usian.config.RedisClient;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentExample;
import com.usian.utils.AdNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Loser
 * @date 2021年11月22日 20:58
 */
@Service
@Transactional
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper tbContentMapper;

    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;
    @Value("${AD_HEIGHT}")
    private Integer AD_HEIGHT;
    @Value("${AD_WIDTH}")
    private Integer AD_WIDTH;
    @Value("${AD_HEIGHTB}")
    private Integer AD_HEIGHTB;
    @Value("${AD_WIDTHB}")
    private Integer AD_WIDTHB;
    @Autowired
    private RedisClient redisClient;
    @Value("${PORTAL_AD_KEY}")
    private String PORTAL_AD_KEY;

    @Override
    public List<AdNode> selectFrontendContentByAD() {
        //查询是否有缓存
        List<AdNode> list = (List<AdNode>) redisClient.hget(PORTAL_AD_KEY, AD_CATEGORY_ID.toString());
        if (list != null){
            return list;
        }

        TbContentExample tbContentExample = new TbContentExample();
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(AD_CATEGORY_ID);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);
        List<AdNode> adNodes = new ArrayList<>();

        for (TbContent tbContent : tbContentList) {
            //创建实体类
            AdNode adNode = new AdNode();

            adNode.setHeight(AD_HEIGHT);
            adNode.setHeightB(AD_HEIGHTB);
            adNode.setWidth(AD_WIDTH);
            adNode.setWidthB(AD_WIDTHB);

            adNode.setSrc(tbContent.getPic());
            adNode.setSrcB(tbContent.getPic2());
            adNode.setHref(tbContent.getUrl());
            adNodes.add(adNode);
        }
        //如果不存在缓存就添加到缓存中
        redisClient.hset(PORTAL_AD_KEY,AD_CATEGORY_ID.toString(),adNodes);
        return adNodes;
    }
}
