package com.usian.controller;

import com.usian.feign.ContentServiceFeign;
import com.usian.pojo.TbContent;
import com.usian.pojo.TbContentCategory;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Loser
 * @date 2021年11月22日 11:44
 */
@RestController
@RequestMapping("/backend/content")
public class ContentCategoryController {

    @Autowired
    private ContentServiceFeign contentServiceFeign;

    @RequestMapping("/selectContentCategoryByParentId")
    public Result selectContentCategoryByParentId(@RequestParam(defaultValue = "0") Long id){
        List<TbContentCategory> list = contentServiceFeign.selectContentCategoryByParentId(id);
        if (list != null){
            return Result.ok(list);
        }
        return Result.error("查询失败");
    }

    @RequestMapping("/insertContentCategory")
    public Result insertContentCategory(TbContentCategory tbContentCategory){
        Integer count = contentServiceFeign.insertContentCategory(tbContentCategory);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    @RequestMapping("/deleteContentCategoryById")
    public Result deleteContentCategoryById(Long categoryId){
        Integer count = contentServiceFeign.deleteContentCategoryById(categoryId);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    @RequestMapping("/updateContentCategory")
    public Result updateContentCategory(TbContentCategory tbContentCategory){
        Integer count = contentServiceFeign.updateContentCategory(tbContentCategory);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("修改失败");
    }

    /**
     * 查询内容管理
     * @param categoryId
     * @param pages
     * @param rows
     * @return
     */
    @RequestMapping("/selectTbContentAllByCategoryId")
    public Result selectTbContentAllByCategoryId(Long categoryId,@RequestParam(defaultValue = "1") Integer pages,@RequestParam(defaultValue = "5") Integer rows){
        PageResult pageResult = contentServiceFeign.selectTbContentAllByCategoryId(categoryId,pages,rows);
        if (pageResult != null){
            return Result.ok(pageResult);
        }
        return Result.error("查询失败");
    }

    /**
     * 添加内容管理
     * @param tbContent
     * @return
     */
    @RequestMapping("/insertTbContent")
    public Result insertTbContent(TbContent tbContent){
        Integer count = contentServiceFeign.insertTbContent(tbContent);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("插入失败");
    }

    @RequestMapping("/deleteContentByIds")
    public Result deleteContentByIds(Long ids){
        Integer count = contentServiceFeign.deleteContentByIds(ids);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }
}
