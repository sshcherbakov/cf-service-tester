package io.pivotal.cf.tester.service;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

public class StateService implements InitializingBean {
	
	private volatile boolean isRabbitUp = false;
	private volatile boolean isRedisUp = false;
	private AtomicLong nextId = new AtomicLong();
	
	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;

	@Value("${max.message.id:10000}")
	private int maxMessageId = 10000;
	
	@Autowired(required=false)
	RedisTemplate<String, String> redisTemplate;


	@PostConstruct
	public void init() {
		resetCheckpoints();
	}

	
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

	public String getNextId() {
		long id = nextId.getAndIncrement();
		if( id == maxMessageId ) {		// only one thread will see maxMessageId
			id = 0;
			nextId.set(id + 1);
			resetCheckpoints();
		}
		return Long.toString(id);
	}
	
	
	private void resetCheckpoints() {
		if( redisTemplate != null ) {
			redisTemplate.boundValueOps(applicationName).getOperations().delete(applicationName);
		}
	}

}
