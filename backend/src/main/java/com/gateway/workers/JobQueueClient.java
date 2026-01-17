package com.gateway.workers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class JobQueueClient {

    private final JedisPool jedisPool;
    private final ObjectMapper objectMapper;

    public JobQueueClient(ObjectMapper objectMapper) {
        String redisUrl = System.getenv().getOrDefault("REDIS_URL", "redis://localhost:6379");
        this.jedisPool = new JedisPool(redisUrl);
        this.objectMapper = objectMapper;
    }

    public void enqueue(String queueName, Object job) {
        try (Jedis jedis = jedisPool.getResource()) {
            String payload = objectMapper.writeValueAsString(job);
            jedis.rpush(queueName, payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue job to " + queueName, e);
        }
    }

    public String blockingPopRaw(String queueName, int timeoutSeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            var res = jedis.blpop(timeoutSeconds, queueName);
            if (res == null || res.size() < 2) {
                return null;
            }
            return res.get(1);
        } catch (Exception e) {
            throw new RuntimeException("Failed to pop job from " + queueName, e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
