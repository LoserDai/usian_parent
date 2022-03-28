package com.usian.controller;

import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.PageResult;
import com.usian.utils.Result;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Loser
 * @date 2021年11月18日 11:56
 */
@RestController
@RequestMapping("/backend/item")
@Api(description = "商品管理")
public class ItemController {
    @Autowired
    private ItemServiceFeign itemServiceFeign;

    /**
     * 根据商品id查询商品
     * @param itemId
     * @return
     */
    @PostMapping(value = "/selectItemInfo")
    @ApiOperation(value="查询商品信息",notes = "按itemId查询商品信息")
    @ApiImplicitParam(name="itemId",dataType = "Integer",value = "商品id")
    @ApiResponses({
            @ApiResponse(code = 200,message = "查询成功",response = TbItem.class),
            @ApiResponse(code = 500,message = "查询失败")
    })
    public TbItem selectItemInfo(Long itemId){
        return itemServiceFeign.selectItemInfo(itemId);
    }

    /**
     * 分页查询
     * @param page
     * @param rows
     * @return
     */
    @GetMapping(value = "/selectTbItemAllByPage")
    @ApiOperation("分页查询商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page", dataType = "String", value = "当前页码"),
            @ApiImplicitParam(name="rows", dataType = "String", value = "每页多少条",defaultValue = "5")
    })
    public Result selectTbItemAllByPage(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer rows){
        PageResult pageResult = itemServiceFeign.selectTbItemAllByPage(Integer.valueOf(page), Integer.valueOf(rows));
        if (pageResult != null && pageResult.getResult() != null && pageResult.getResult().size() > 0){
            return Result.ok(pageResult);
        }
        return Result.error("查询无果");
    }

    /**
     * 添加商品
     * @param tbItem
     * @return
     */
    @RequestMapping(value = "/insertTbItem", method = RequestMethod.POST)
    @ApiOperation("新增商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "desc", type = "String", value = "商品描述"),
            @ApiImplicitParam(name = "itemParams", type = "String", value = "商品规格参数")
    })
    public Result insertTbItem(TbItem tbItem, @RequestParam String desc, @RequestParam String itemParams){
        Integer count =  itemServiceFeign.insertTbItem(tbItem,desc,itemParams);
        if (count == 3){
            return Result.ok();
        }
        return Result.error("添加失败");
    }

    /**
     * 删除商品: 把商品的状态改成删除
     * @param itemId
     * @return
     */
    @RequestMapping(value = "/deleteItemById")
    public Result deleteItemById(Long itemId){
        Integer count = itemServiceFeign.deleteItemById(itemId);
        if (count > 0){
            return Result.ok();
        }
        return Result.error("删除失败!");
    }

    /**
     * 回显
     * @param itemId
     * @return
     */
    @RequestMapping("/preUpdateItem")
    public Result preUpdateItem(Long itemId){
        Map<String,Object> map = itemServiceFeign.preUpdateItem(itemId);
        if (map.size() > 0){
            return Result.ok(map);
        }
        return Result.error("查询失败");
    }

    /**
     * 修改商品信息
     * @param tbItem
     * @param desc
     * @param itemParams
     * @return
     */
    @RequestMapping("/updateTbItem")
    public Result updateTbItem(TbItem tbItem, String desc,String itemParams){
        Integer count = itemServiceFeign.updateTbItem(tbItem, desc, itemParams);
        if (count == 3){
            return Result.ok();
        }
        return Result.error("修改失败");
    }
}
