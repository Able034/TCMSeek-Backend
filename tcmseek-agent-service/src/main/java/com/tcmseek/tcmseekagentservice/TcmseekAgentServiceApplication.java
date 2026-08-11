package com.tcmseek.tcmseekagentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class TcmseekAgentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmseekAgentServiceApplication.class, args);
        System.out.println("TcmseekAgentServiceApplication started...");
    }

}
