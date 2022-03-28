package com.usian.controller;

import com.usian.pojo.SearchItem;
import com.usian.service.SearchItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Loser
 * @date 2021年11月26日 21:19
 */
@RestController
@RequestMapping("/service/searchItem")
public class SearchItemController {

    @Autowired
    private SearchItemService searchItemService;

    @RequestMapping("/importAll")
    public Boolean importAll(){
        return searchItemService.importAll();
    }

    @RequestMapping("/list")
    public List<SearchItem> list(String q,  Long page, Integer pageSize){
        return searchItemService.list(q,page,pageSize);
    }
}
