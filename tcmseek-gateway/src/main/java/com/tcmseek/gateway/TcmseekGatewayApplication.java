package com.tcmseek.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TcmseekGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmseekGatewayApplication.class, args);
        System.out.println("tcmseek-gateway启动成功");
    }
}
