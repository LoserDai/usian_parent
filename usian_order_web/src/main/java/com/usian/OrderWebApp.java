package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Loser
 * @date 2021年12月03日 14:24
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrderWebApp {
    public static void main(String[] args) {
        SpringApplication.run(OrderWebApp.class);
    }
}
