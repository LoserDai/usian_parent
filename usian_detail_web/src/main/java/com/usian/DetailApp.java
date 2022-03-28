package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Loser
 * @date 2021年11月30日 11:35
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class DetailApp {
    public static void main(String[] args) {
        SpringApplication.run(DetailApp.class);
    }
}
