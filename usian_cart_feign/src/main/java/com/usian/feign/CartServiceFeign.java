package com.usian.feign;

import com.usian.pojo.TbItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("usian-cart-service")
public interface CartServiceFeign {

    @RequestMapping("/service/cart/getCartFromRedis")
    Map<String, TbItem> getCartFromRedis(@RequestParam Long userId);

    @RequestMapping("/service/cart/addCartToRedis")
    void addCartToRedis(@RequestBody Map<String, TbItem> cart, @RequestParam Long userId);
}
