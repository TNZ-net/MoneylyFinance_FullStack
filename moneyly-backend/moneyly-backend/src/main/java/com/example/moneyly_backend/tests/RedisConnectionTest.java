package com.example.moneyly_backend.tests;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
@Component
public class RedisConnectionTest implements CommandLineRunner {
    private final StringRedisTemplate redisTemplate;
    public RedisConnectionTest(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    @Override
    public void run(String... args) {
        redisTemplate.opsForValue().set("moneyly:connection-test", "connected");

        String result = redisTemplate.opsForValue()
                .get("moneyly:connection-test");
        System.out.println("REDIS TEST RESULT: " + result);
    }
}