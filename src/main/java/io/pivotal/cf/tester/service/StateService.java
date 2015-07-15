package io.pivotal.cf.tester.service;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class StateService implements InitializingBean {
	
	private volatile boolean isRabbitUp = false;
	private volatile boolean isRedisUp = false;
	
	@Autowired(required=false)
	RedisTemplate<String, Map<String,Object>> redisTemplate;


	public boolean isRabbitUp() {
		return isRabbitUp;
	}

	public void setRabbitUp() {
		this.isRabbitUp = true;
	}
	
	public void setRabbitDown() {
		this.isRabbitUp = false;
	}
	
	
	public boolean isRedisUp() {
		return isRedisUp;
	}

	public void setRedisUp() {
		this.isRedisUp = true;
	}
		
	public void setRedisDown() {
		this.isRedisUp = false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		isRedisUp = redisTemplate != null;
	}
	
}
