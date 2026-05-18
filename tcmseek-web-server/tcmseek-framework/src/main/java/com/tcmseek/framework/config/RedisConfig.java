package com.tcmseek.framework.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * redis配置
 * 
 * @author ruoyi
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport
{

    /**
     * 配置并创建Redis缓存管理器，用于管理应用层面的缓存策略。
     * 设置默认的缓存过期时间为1小时，使用String序列化器处理缓存键，
     * 使用FastJson2序列化器处理缓存值，并禁用空值缓存。
     * 同时为特定的缓存名称配置独立的过期时间策略。
     *
     * @param factory Redis连接工厂，用于创建与Redis服务器的连接
     * @return CacheManager 配置完成的Redis缓存管理器实例
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory){
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new FastJson2JsonRedisSerializer<Object>(Object.class)))
                .disableCachingNullValues();

        //不同cache设置不同过期时间
        Map<String,RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        // ========== 中药相关缓存 ==========
        // 中药详情缓存 - 2小时
        cacheConfigurations.put("herb:detail",config.entryTtl(Duration.ofHours(2)));
        // 中药列表第一页缓存 - 6小时
        cacheConfigurations.put("herb:list:page1",config.entryTtl(Duration.ofHours(6)));
        // ========== 其他实体列表第一页缓存 ==========
        // 化合物列表
        cacheConfigurations.put("compound:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 方剂列表
        cacheConfigurations.put("prescription:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 症状列表
        cacheConfigurations.put("symptom:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 证候列表
        cacheConfigurations.put("syndrome:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 基因列表
        cacheConfigurations.put("gene:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 通路列表
        cacheConfigurations.put("pathway:list:page1",config.entryTtl(Duration.ofHours(6)));
        // 表型列表
        cacheConfigurations.put("phenotype:list:page1",config.entryTtl(Duration.ofHours(6)));

        //化合物详情缓存2小时
        cacheConfigurations.put("compound:detail",config.entryTtl(Duration.ofHours(2)));
        //疾病详情缓存2小时
        cacheConfigurations.put("disease:detail",config.entryTtl(Duration.ofHours(2)));
        //基因详情缓存2小时
        cacheConfigurations.put("gene:detail",config.entryTtl(Duration.ofHours(2)));
        //医案详情缓存2小时
        cacheConfigurations.put("medicalCase:detail",config.entryTtl(Duration.ofHours(2)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

    }


    @Bean
    @SuppressWarnings(value = { "unchecked", "rawtypes" })
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory)
    {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        FastJson2JsonRedisSerializer serializer = new FastJson2JsonRedisSerializer(Object.class);

        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        // Hash的key也采用StringRedisSerializer的序列化方式
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public DefaultRedisScript<Long> limitScript()
    {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(limitScriptText());
        redisScript.setResultType(Long.class);
        return redisScript;
    }

    /**
     * 限流脚本
     */
    private String limitScriptText()
    {
        return "local key = KEYS[1]\n" +
                "local count = tonumber(ARGV[1])\n" +
                "local time = tonumber(ARGV[2])\n" +
                "local current = redis.call('get', key);\n" +
                "if current and tonumber(current) > count then\n" +
                "    return tonumber(current);\n" +
                "end\n" +
                "current = redis.call('incr', key)\n" +
                "if tonumber(current) == 1 then\n" +
                "    redis.call('expire', key, time)\n" +
                "end\n" +
                "return tonumber(current);";
    }
}
