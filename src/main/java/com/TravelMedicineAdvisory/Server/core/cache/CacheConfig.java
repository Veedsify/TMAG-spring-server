package com.TravelMedicineAdvisory.Server.core.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return defaultRedisCacheConfiguration();
    }

    @Bean
    public CacheManager cacheManager(
            @Value("${app.cache.enabled:true}") boolean cacheEnabled,
            @Value("${app.cache.provider:redis}") String cacheProvider,
            RedisConnectionFactory connectionFactory) {
        if (!cacheEnabled) {
            return new NoOpCacheManager();
        }
        if ("memory".equalsIgnoreCase(cacheProvider)) {
            return new ConcurrentMapCacheManager(CacheNames.ALL);
        }
        return redisCacheManager(connectionFactory);
    }

    private static RedisCacheConfiguration defaultRedisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    private static RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration oneHour = defaultRedisCacheConfiguration();
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CacheNames.COUNTRIES, oneHour.entryTtl(Duration.ofHours(24)));
        perCache.put(CacheNames.CURRENCIES, oneHour.entryTtl(Duration.ofHours(24)));
        perCache.put(CacheNames.ONBOARDING_QUESTIONS, oneHour.entryTtl(Duration.ofHours(24)));
        perCache.put(CacheNames.EBOOKS, oneHour.entryTtl(Duration.ofMinutes(30)));
        perCache.put(CacheNames.BLOG_POSTS, oneHour.entryTtl(Duration.ofMinutes(30)));
        perCache.put(CacheNames.SYSTEM_SETTINGS, oneHour.entryTtl(Duration.ofMinutes(15)));
        perCache.put(CacheNames.COMPANY_SETTINGS, oneHour.entryTtl(Duration.ofMinutes(15)));
        perCache.put(CacheNames.COMPANY_CODE_VALIDATION, oneHour.entryTtl(Duration.ofMinutes(5)));
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(oneHour)
                .withInitialCacheConfigurations(perCache)
                .build();
    }
}
