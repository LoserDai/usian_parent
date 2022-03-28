package com.usian.service;

import com.usian.mapper.DeDuplicationMapper;
import com.usian.pojo.DeDuplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Loser
 * @date 2021年12月06日 17:04
 */
@Service
@Transactional
public class DeDuplicationServiceImpl implements DeDuplicationService {

    @Autowired
    private DeDuplicationMapper deDuplicationMapper;

    @Override
    public void addDeDuplication(String txNo) {
        DeDuplication deDuplication = new DeDuplication();
        deDuplication.setCreateTime(new Date());
        deDuplication.setTxNo(txNo);
        deDuplicationMapper.insertSelective(deDuplication);
    }

    @Override
    public DeDuplication selectDeDuplicationByTxNo(String txNo) {
        return deDuplicationMapper.selectByPrimaryKey(txNo);
    }
}
