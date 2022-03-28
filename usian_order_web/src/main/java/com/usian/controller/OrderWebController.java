package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.OrderServiceFeign;
import com.usian.pojo.OrderInfo;
import com.usian.pojo.TbItem;
import com.usian.pojo.TbOrder;
import com.usian.pojo.TbOrderShipping;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Loser
 * @date 2021年12月03日 14:23
 */
@RestController
@RequestMapping("/frontend/order")
public class OrderWebController {

    @Autowired
    private CartServiceFeign cartServiceFeign;
    @Autowired
    private OrderServiceFeign orderServiceFeign;

    @RequestMapping("/goSettlement")
    public Result goSettlement(String[] ids, String userId, String token){
        //获取购物车
        Map<String, TbItem> cart = cartServiceFeign.getCartFromRedis(Long.valueOf(userId));
        //从购物车中获取选中的商品
        if (cart == null || cart.size() == 0){
            return Result.error("查询失败");
        }
        ArrayList<TbItem> tbItemList = new ArrayList<>();
        for (String id : ids) {
            TbItem tbItem = cart.get(id);
            tbItemList.add(tbItem);
        }
        return Result.ok(tbItemList);
    }
    @RequestMapping("/insertOrder")
    public Result insertOrder(String orderItem, TbOrder tbOrder, TbOrderShipping tbOrderShipping){
        //因为一个request中只包含一个request body. 所以feign不支持多个@RequestBody。
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderItem(orderItem);
        orderInfo.setTbOrder(tbOrder);
        orderInfo.setTbOrderShipping(tbOrderShipping);
        //向前端返回订单Id
        Long orderId = orderServiceFeign.insertOrder(orderInfo);
        if (orderId != null){
            return Result.ok(orderId);
        }
            return Result.error("查询失败");
    }
}
