package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Loser
 * @date 2021年12月02日 14:18
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class CartWebApp {
    public static void main(String[] args) {
        SpringApplication.run(CartWebApp.class);
    }
}
