package com.usian.controller;

import com.usian.pojo.TbItem;
import com.usian.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Loser
 * @date 2021年12月02日 16:35
 */
@RestController
@RequestMapping("/service/cart")
public class CartServiceController {

    @Autowired
    private CartService cartService;

    @RequestMapping("/getCartFromRedis")
    public Map<String, TbItem> getCartFromRedis(Long userId){
        return cartService.getCartFromRedis(userId);
    }

    @RequestMapping("/addCartToRedis")
    public void addCartToRedis(@RequestBody Map<String, TbItem> cart, Long userId){
        cartService.addCartToRedis(cart, userId);
    }
}
