package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItemParam;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Loser
 * @date 2021年11月18日 19:13
 */
@RestController
@RequestMapping("/backend/itemParam")
public class ItemParamController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 通过类目的Id查询模板id
     * @param itemCatId
     * @return
     */
    @RequestMapping("selectItemParamByItemCatId/{itemCatId}")
    public Result selectItemParamByItemCatId(@PathVariable Integer itemCatId){
        TbItemParam tbItemParam = itemServiceFeign.selectItemParamByItemCatId(itemCatId);
            if (tbItemParam != null){
                return Result.ok(tbItemParam);
            }
            return Result.error("查无此人!!!");
    }

    @RequestMapping("/insertItemParam")
    public Result insertItemParam(TbItemParam tbItemParam){
        Integer count = itemServiceFeign.insertItemParam(tbItemParam);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("插入失败");
    }

    @RequestMapping("/deleteItemParamById")
    public Result deleteItemParamById(Integer id){
        Integer count = itemServiceFeign.deleteItemParamById(id);
        if (count == 1){
            return Result.ok();
        }
        return Result.error("删除失败");
    }

    @RequestMapping("/selectItemParamAll")
    public Result selectItemParamAll(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "5") Integer rows){
        PageResult pageResult = itemServiceFeign.selectItemParamAll(page,rows);
        if (pageResult != null){
            return Result.ok(pageResult);
        }
        return Result.error("查询失败");
    }
}
