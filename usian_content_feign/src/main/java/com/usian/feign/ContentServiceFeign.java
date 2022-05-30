package com.usian.feign;

import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.AdNode;
import com.usian.utils.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "usian-content-service")
public interface ContentServiceFeign {

    @RequestMapping("/service/content/selectContentCategoryByParentId")
    public List<TbContentCategory> selectContentCategoryByParentId(@RequestParam Long id);

    @RequestMapping("/service/content/insertContentCategory")
    Integer insertContentCategory(@RequestBody TbContentCategory tbContentCategory);

    @RequestMapping("/service/content/deleteContentCategoryById")
    Integer deleteContentCategoryById(@RequestParam Long categoryId);

    @RequestMapping("/service/content/updateContentCategory")
    Integer updateContentCategory(@RequestBody TbContentCategory tbContentCategory);

    /**
     * 分页查询
     * @param categoryId
     * @param pages
     * @param rows
     * @return
     */
    @RequestMapping("/service/content/selectTbContentAllByCategoryId")
    PageResult selectTbContentAllByCategoryId(@RequestParam Long categoryId, @RequestParam Integer pages, @RequestParam Integer rows);

    /**
     * 添加内容管理
     * @param tbContent
     * @return
     */
    @RequestMapping("/service/content/insertTbContent")
    Integer insertTbContent(@RequestBody TbContent tbContent);

    @RequestMapping("/service/content/deleteContentByIds")
    Integer deleteContentByIds(@RequestParam Long ids);

    @RequestMapping("/service/content/selectFrontendContentByAD")
    List<AdNode> selectFrontendContentByAD();
}
