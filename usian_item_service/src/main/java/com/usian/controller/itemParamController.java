package com.usian.controller;


import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamItem;
import com.usian.service.ItemParamService;
import com.usian.utils.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Loser
 * @date 2021年11月18日 19:20
 */
@RestController
@RequestMapping("/service/itemParam")
public class itemParamController {
    @Autowired
    private ItemParamService itemParamService;

    /**
     * 查询模板参数
     * @param itemCatId
     * @return
     */
    @RequestMapping("/selectItemParamByItemCatId/{itemCatId}")
    public TbItemParam selectItemParamByItemCatId(@PathVariable Integer itemCatId){
        TbItemParam tbItemParam = itemParamService.selectItemParamByItemCatId(itemCatId);
        return tbItemParam;
    }
    @RequestMapping("/insertItemParam")
    public Integer insertItemParam(@RequestBody TbItemParam tbItemParam){
         return itemParamService.insertItemParam(tbItemParam);
    }

    @RequestMapping("/deleteItemParamById")
    public Integer deleteItemParamById(Integer id){
        return itemParamService.deleteItemParamById(id);
    }

    @RequestMapping("/selectItemParamAll")
    public PageResult selectItemParamAll(Integer page, Integer rows){
        return itemParamService.selectItemParamAll(page,rows);
    }

    @RequestMapping("/selectTbItemParamItemByItemId")
    public TbItemParamItem selectItemDescByItemId(Long itemId){
        return itemParamService.selectTbItemParamItemByItemId(itemId);
    }
}
