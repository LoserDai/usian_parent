package com.usian.service;

import com.usian.pojo.SearchItem;

import java.io.IOException;
import java.util.List;

public interface SearchItemService {

    Boolean importAll();

    List<SearchItem> list(String q, Long page, Integer pageSize);

    void insertDocument(String msg) throws IOException;
}
