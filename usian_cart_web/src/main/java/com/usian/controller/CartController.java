package com.usian.controller;

import com.usian.feign.CartServiceFeign;
import com.usian.feign.ItemServiceFeign;
import com.usian.pojo.TbItem;
import com.usian.utils.CookieUtils;
import com.usian.utils.JsonUtils;
import com.usian.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Loser
 * @date 2021年12月02日 14:25
 */
@RestController
@RequestMapping("/frontend/cart/")
public class CartController {

    @Value("${CART_COOKIE_KEY}")
    private String CART_COOKIE_KEY;
    @Value("${CART_COOKIE_EXPIRE}")
    private int CART_COOKIE_EXPIRE;
    @Autowired
    private ItemServiceFeign itemServiceFeign;
    @Autowired
    private CartServiceFeign cartServiceFeign;

    /**
     * 添加购物车
     * @param itemId
     * @param userId
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/addItem")
    public Result addItem(String itemId, Long userId, HttpServletRequest request, HttpServletResponse response){
        //未登录
        try {
            if (userId == null){
                //1、从cookie中把车拿出来
                Map<String, TbItem> cart = getCartFromCookie(request);
                //2、把商品添加到车里
                addItemToCart(cart, itemId,1);
                //3、把车添加到cookie
                addCartToCookie(cart, request, response);
            }else {
                //已登录
                //1、从redis中把车拿出来
                Map<String, TbItem> cart = cartServiceFeign.getCartFromRedis(userId);
                //2、把商品添加到车里
                addItemToCart(cart, itemId,1);
                //3、把车添加到cookie
                cartServiceFeign.addCartToRedis(cart,userId);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("添加失败");
        }
    }

    @RequestMapping("/showCart")
    public Result showCart(Long userId, HttpServletRequest request){
        try{
            ArrayList<TbItem> cartList = new ArrayList<>();
            Map<String, TbItem> cart = null;
            //未登录
            if (userId == null){
                cart = getCartFromCookie(request);
            }else{
                //已登录
                cart = cartServiceFeign.getCartFromRedis(userId);
            }
            Set<String> keySet = cart.keySet();
            for (String key : keySet) {
                TbItem tbItem = cart.get(key);
                cartList.add(tbItem);
            }
            return Result.ok(cartList);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("查询失败");
        }
    }

    @RequestMapping("/updateItemNum")
    public Result updateItemNum(Long userId, String itemId, int num, HttpServletRequest request, HttpServletResponse response){
        //设置购物车中的商品数目
        try {
            if (userId == null){
                //未登录
                //1、从cookie中把车拿出来
                Map<String, TbItem> cart = getCartFromCookie(request);
                //2、把商品添加到车里
                addItemToCart(cart, itemId, num);
                //3、把车添加到cookie
                addCartToCookie(cart, request, response);
            }else {
                //已登录
                //1、从redis中把车拿出来
                Map<String, TbItem> cartRedis = cartServiceFeign.getCartFromRedis(userId);
                //2、修改车里商品数量
                TbItem tbItem = cartRedis.get(itemId);
                tbItem.setNum(num);
                cartRedis.put(itemId, tbItem);
                //3、把车添加到cookie
                cartServiceFeign.addCartToRedis(cartRedis,userId);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("修改失败!");
        }
    }

    @RequestMapping("/deleteItemFromCart")
    public Result deleteItemFromCart(Long userId, String itemId, HttpServletRequest request, HttpServletResponse response){
        try {
            if (userId == null){
                //未登录
                //1、从cookie中把车拿出来
                Map<String, TbItem> cart = getCartFromCookie(request);
                //2、把商品从车里删除
                cart.remove(itemId);
                //3、把车添加到cookie
                addCartToCookie(cart, request, response);
            }else{
                //已登录
                Map<String, TbItem> cartRedis = cartServiceFeign.getCartFromRedis(userId);
                //删除redis中相关的商品
                cartRedis.remove(itemId);
                //把车添加到redis中
                cartServiceFeign.addCartToRedis(cartRedis, userId);
            }
            return Result.ok();
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("删除失败");
        }
    }

    /**
     * 把车放入到cookie
     * @param cart
     * @param request
     * @param response
     */
    private void addCartToCookie(Map<String, TbItem> cart, HttpServletRequest request, HttpServletResponse response) {
        //请求, 响应, 存在map的key, 将cart对象转换成json串, 设置失效时间, 是否编码
        CookieUtils.setCookie(request,response,CART_COOKIE_KEY,JsonUtils.objectToJson(cart),CART_COOKIE_EXPIRE,true);
    }

    //把商品放入到车中:首先先查询该商品的信息,然后设置商品的num为 1
    private void addItemToCart(Map<String, TbItem> cart, String itemId,int num) {
        TbItem tbItem = itemServiceFeign.selectItemInfo(Long.valueOf(itemId));
        tbItem.setNum(num);
        cart.put(itemId,tbItem);
    }

    //从Cookie中把Car拿出来
    private Map<String, TbItem> getCartFromCookie(HttpServletRequest request) {
        String cartJson = CookieUtils.getCookieValue(request, CART_COOKIE_KEY, true);
        if (cartJson != null){
            //将json对象转换成map返回回去
            return JsonUtils.jsonToMap(cartJson,TbItem.class);
        }
            return new HashMap<>();
    }
}
