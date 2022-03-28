package com.usian.service;

import com.usian.pojo.TbItemParam;
import com.usian.pojo.TbItemParamItem;
import com.usian.utils.PageResult;

public interface ItemParamService {

    TbItemParam selectItemParamByItemCatId(Integer itemCatId);

    Integer insertItemParam(TbItemParam tbItemParam);

    Integer deleteItemParamById(Integer id);

    PageResult selectItemParamAll(Integer page, Integer rows);

    TbItemParamItem selectTbItemParamItemByItemId(Long itemId);
}
