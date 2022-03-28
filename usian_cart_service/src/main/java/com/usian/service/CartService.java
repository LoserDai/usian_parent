package com.usian.service;

import com.usian.pojo.TbItem;

import java.util.Map;

public interface CartService {

    Map<String, TbItem> getCartFromRedis(Long userId);

    void addCartToRedis(Map<String, TbItem> cart, Long userId);
}
