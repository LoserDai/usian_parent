package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author Loser
 * @date 2021年12月02日 14:19
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CartServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApp.class);
    }
}
