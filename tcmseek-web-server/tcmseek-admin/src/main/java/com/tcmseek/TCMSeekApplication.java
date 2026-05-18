package com.tcmseek;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@MapperScan({"com.tcmseek.**.dao", "com.tcmseek.**.mapper"})
@EnableDiscoveryClient
@EnableScheduling
@EnableCaching
public class TCMSeekApplication
{
    public static void main(String[] args)
    {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(TCMSeekApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  TCMSeeek启动成功   ლ(´ڡ`ლ)ﾞ  \n" );
    }
}
