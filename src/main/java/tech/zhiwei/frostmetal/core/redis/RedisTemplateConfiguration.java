package tech.zhiwei.frostmetal.core.redis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis 配置类
 *
 * @author LIEN
 * @since 2024/9/12
 */
@Configuration
public class RedisTemplateConfiguration {

    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // key 序列化
        RedisKeySerializer redisKeySerializer = new RedisKeySerializer();
        redisTemplate.setKeySerializer(redisKeySerializer);
        redisTemplate.setHashKeySerializer(redisKeySerializer);
        // value 序列化
//        RedisSerializer<Object> redisSerializer = new JdkSerializationRedisSerializer();
        RedisSerializer<Object> redisSerializer = new GenericJackson2JsonRedisSerializer("@class");
        redisTemplate.setValueSerializer(redisSerializer);
        redisTemplate.setHashValueSerializer(redisSerializer);

        return redisTemplate;
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisUtil redisUtils(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtil(redisTemplate);
    }
}
