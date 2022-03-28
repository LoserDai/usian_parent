package com.usian.service;

import com.usian.pojo.OrderInfo;

public interface OrderService {
    Long insertOrder(OrderInfo orderInfo);

    void closeTimeOutOrder();

    void scanAndSendOfLocalMessage();
}
