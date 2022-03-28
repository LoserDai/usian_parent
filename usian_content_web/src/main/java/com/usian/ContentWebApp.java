package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Loser
 * @date 2021年11月22日 11:42
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ContentWebApp {
    public static void main(String[] args) {
        SpringApplication.run(ContentWebApp.class);
    }
}
