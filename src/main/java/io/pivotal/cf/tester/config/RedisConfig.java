package io.pivotal.cf.tester.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
	
	@Bean
	public RedisSerializer<String> redisKeySerializer() {
		return new StringRedisSerializer();
	}
	
	@Bean
	public RedisTemplate<String, Object> redisTemplate(
			RedisConnectionFactory connectionFactory, RedisSerializer<String> keySerializer) {
		
		RedisTemplate<String, Object> rt = new RedisTemplate<>();
		rt.setConnectionFactory(connectionFactory);
		rt.setKeySerializer(keySerializer);
		rt.setHashKeySerializer(keySerializer);
		return rt;
	}

	@Bean
	public RedisTemplate<String, Map<String,Object>> redisConfigTemplate(
			RedisConnectionFactory connectionFactory, RedisSerializer<String> keySerializer) {
		
		RedisTemplate<String, Map<String,Object>> rt = new RedisTemplate<>();
		rt.setConnectionFactory(connectionFactory);
		rt.setKeySerializer(keySerializer);
		rt.setHashKeySerializer(keySerializer);
		return rt;
	}
	
}