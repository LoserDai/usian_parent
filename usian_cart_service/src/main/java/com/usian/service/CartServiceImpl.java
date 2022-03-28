package com.usian.service;

import com.usian.config.RedisClient;
import com.usian.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Loser
 * @date 2021年12月02日 16:41
 */
@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisClient redisClient;

    @Value("${CART_REDIS_KEY}")
    private String CART_REDIS_KEY;

    @Override
    public Map<String, TbItem> getCartFromRedis(Long userId) {
        Map<String, TbItem> map = (Map<String, TbItem>) redisClient.hget(CART_REDIS_KEY, String.valueOf(userId));
        if (map != null){
            return map;
        }
        return new HashMap<>();
    }

    @Override
    public void addCartToRedis(Map<String, TbItem> cart, Long userId) {
        redisClient.hset(CART_REDIS_KEY, userId.toString(), cart);
    }
}
