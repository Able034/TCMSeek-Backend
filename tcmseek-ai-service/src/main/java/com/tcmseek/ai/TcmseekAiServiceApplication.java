package com.tcmseek.ai;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDiscoveryClient
@SpringBootApplication
public class TcmseekAiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TcmseekAiServiceApplication.class, args);
        System.out.println("TcmseekAiServiceApplication started...");
    }
}
