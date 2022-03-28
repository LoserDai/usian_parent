package com.usian.job;

import com.usian.config.RedisClient;
import com.usian.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Date;

/**
 * @author Loser
 * @date 2021年12月04日 14:54
 */
@Component
public class OrderJob {

    @Value("${SETNX_ORDER_LOCK_KEY}")
    private String SETNX_ORDER_LOCK_KEY;
    @Value("${LOCK_TIMES}")
    private Long LOCK_TIMES;
    @Autowired
    private RedisClient redisClient;

    @Autowired
    private OrderService orderService;

    public void closeTimeOutOrder(){
        //采用分布式锁方式任务重复执行
        try{
            String address = InetAddress.getLocalHost().getHostAddress();
            if(redisClient.setnx(SETNX_ORDER_LOCK_KEY, address,LOCK_TIMES)){
                System.out.println("服务执行! 关闭超时订单, 执行时间: " + new Date());
                //关闭超时订单
                orderService.closeTimeOutOrder();
                //扫描状态是1的消息并再次发送
                System.out.println("扫描本地消息表： " + new Date());
                orderService.scanAndSendOfLocalMessage();
            }else {
                address = (String) redisClient.get(SETNX_ORDER_LOCK_KEY);
                System.out.println("==========机器："+ address +" 占用分布式锁，任务已然执行了=============");
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //预防死锁
            redisClient.del(SETNX_ORDER_LOCK_KEY);
        }
    }
}
