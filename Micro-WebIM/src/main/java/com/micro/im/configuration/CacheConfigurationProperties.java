package com.micro.im.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 缓存配置 class
 *
 * @author Mr.zxb
 * @date 2019-10-28 10:41
 */
@ConfigurationProperties(prefix = "cache")
@Data
public class CacheConfigurationProperties {

    private long timeoutSeconds = 60;
    private int redisPort = 6379;
    @Value("${spring.redis.host}")
    private String redisHost;
    private Map<String, Long> cacheExpirations = new ConcurrentHashMap<>();
}
