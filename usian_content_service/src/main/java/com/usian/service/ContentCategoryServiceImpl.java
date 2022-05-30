package com.usian.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.usian.config.RedisClient;
import com.usian.mapper.TbContentCategoryMapper;
import com.usian.mapper.TbContentMapper;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentCategory;
import com.usian.pojo.TbContentCategoryExample;
import com.usian.pojo.TbContentExample;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author Loser
 * @date 2021年11月22日 12:52
 */
@Service
@Transactional
public class ContentCategoryServiceImpl implements ContentCategoryService{

    @Autowired
    private TbContentCategoryMapper tbContentCategoryMapper;
    @Autowired
    private TbContentMapper tbContentMapper;
    @Autowired
    private RedisClient redisClient;
    @Value("${PORTAL_AD_KEY}")
    private String PORTAL_AD_KEY;
    @Value("${AD_CATEGORY_ID}")
    private Long AD_CATEGORY_ID;

    /**
     * 根据id查询ContentCategory
     * @param id
     * @return
     */
    @Override
    public List<TbContentCategory> selectContentCategoryByParentId(Long id) {
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(id);
        List<TbContentCategory> tbContentCategories = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);

        return tbContentCategories;
    }

    /**
     * 添加ContentCategory
     * @param tbContentCategory
     * @return
     */
    @Override
    public Integer insertContentCategory(TbContentCategory tbContentCategory) {
        //1、添加内容分类
        tbContentCategory.setCreated(new Date());
        tbContentCategory.setUpdated(new Date());
        tbContentCategory.setIsParent(false);
        tbContentCategory.setSortOrder(1);
        tbContentCategory.setStatus(1);
        int insert = tbContentCategoryMapper.insert(tbContentCategory);

        //2、如果他爹不是爹，要把他爹改成爹
        //2.1、查询当前新节点的父节点
        TbContentCategory tbCategory = tbContentCategoryMapper.selectByPrimaryKey(tbContentCategory.getParentId());
        //2.2、判断当前父节点是否是叶子节点
        if (!tbCategory.getIsParent()){
            tbCategory.setIsParent(true);
            tbCategory.setUpdated(new Date());
            tbContentCategoryMapper.updateByPrimaryKey(tbCategory);
        }
        return insert;
    }

    @Override
    public Integer deleteContentCategoryById(Long categoryId) {
        //查询当前的父节点
        TbContentCategory tbContentCategory = tbContentCategoryMapper.selectByPrimaryKey(categoryId);
        //如果该节点为父节点,则不允许删除,否则删除
        if (tbContentCategory.getIsParent()){
            return 0;
        }
        //否则删除
        Integer count = tbContentCategoryMapper.deleteByPrimaryKey(categoryId);
        //删除之后如果父节点没有孩子,则修改他的 isParent 为 false
        //查询该父节点是否还有孩子
        TbContentCategoryExample tbContentCategoryExample = new TbContentCategoryExample();
        TbContentCategoryExample.Criteria criteria = tbContentCategoryExample.createCriteria();
        criteria.andParentIdEqualTo(categoryId);
        List<TbContentCategory> list = tbContentCategoryMapper.selectByExample(tbContentCategoryExample);
        //如果没有
        if (list.size() == 0){
            TbContentCategory tbContCategoryParent = new TbContentCategory();
            tbContCategoryParent.setUpdated(new Date());
            tbContCategoryParent.setIsParent(false);
            tbContCategoryParent.setId(tbContentCategory.getParentId());
            int i = tbContentCategoryMapper.updateByPrimaryKey(tbContCategoryParent);
        }
        return count;
    }

    @Override
    public Integer updateContentCategory(TbContentCategory tbContentCategory) {
        return tbContentCategoryMapper.updateByPrimaryKeySelective(tbContentCategory);
    }

    @Override
    public PageResult selectTbContentAllByCategoryId(Long categoryId, Integer pages, Integer rows) {
        PageHelper.startPage(pages,rows);

        TbContentExample tbContentExample = new TbContentExample();
        TbContentExample.Criteria criteria = tbContentExample.createCriteria();
        criteria.andCategoryIdEqualTo(categoryId);
        List<TbContent> tbContentList = tbContentMapper.selectByExample(tbContentExample);

        PageInfo<TbContent> pageInfo = new PageInfo<>(tbContentList);
        PageResult pageResult = new PageResult();
        pageResult.setResult(tbContentList);
        //当前页
        pageResult.setPageIndex(pageInfo.getPageNum());
        pageResult.setTotalPage(Long.valueOf(pageInfo.getPages()));

        return pageResult;
    }

    /**
     * 添加TbContent
     * @param tbContent
     * @return
     */
    @Override
    public Integer insertTbContent(TbContent tbContent) {
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return tbContentMapper.insertSelective(tbContent);
    }

    /**
     * 删除Content
     * @param ids
     * @return
     */
    @Override
    public Integer deleteContentByIds(Long ids) {
        redisClient.hdel(PORTAL_AD_KEY,AD_CATEGORY_ID.toString());
        return tbContentMapper.deleteByPrimaryKey(ids);
    }
}
