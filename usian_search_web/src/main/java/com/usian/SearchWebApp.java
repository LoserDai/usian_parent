package com.usian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author Loser
 * @date 2021年11月26日 21:02
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class SearchWebApp {
    public static void main(String[] args) {
        SpringApplication.run(SearchWebApp.class);
    }
}
