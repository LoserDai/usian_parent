package com.usian.service;

import com.usian.pojo.DeDuplication;

public interface DeDuplicationService {
    void addDeDuplication(String txNo);

    DeDuplication selectDeDuplicationByTxNo(String txNo);
}
