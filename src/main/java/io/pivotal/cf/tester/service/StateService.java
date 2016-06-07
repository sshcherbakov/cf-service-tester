package io.pivotal.cf.tester.service;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StateService implements InitializingBean {
	private static Logger log = LoggerFactory.getLogger(StateService.class);

	private volatile boolean isRabbitUp = false;
	private volatile boolean isRedisUp = false;
	private volatile boolean isDatabaseUp = false;
	private AtomicLong nextId = new AtomicLong();
	
	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;

	@Value("${max.message.id:10000}")
	private int maxMessageId = 10000;
	
	@Autowired(required=false)
	RedisTemplate<String, String> redisTemplate;

	@Autowired
	JdbcChecker jdbcChecker;
	
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

	public boolean isDatabaseUp() {
		return jdbcChecker.checkDatabaseAvailability();
	}

	public void setRedisUp() {
		this.isRedisUp = true;
	}
		
	public void setRedisDown() {
		this.isRedisUp = false;
	}
	
	public void setDatabaseUp(){
		this.isDatabaseUp = true;
	}

	public void setDatabaseDown(){
		this.isDatabaseUp = false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( redisTemplate == null ) {
			isRedisUp = false;
			return;
		}
		try {
			isRedisUp = redisTemplate.getConnectionFactory().getConnection() != null;
		}
		catch(Exception ex) {
			isRedisUp = false;
		}
	}

	public long getNextId() {
		long id = nextId.getAndIncrement();
		if( id == maxMessageId ) {		// only one thread will see maxMessageId
			id = 0;
			nextId.set(id + 1);
			resetCheckpoints();
		}
		return id;
	}
	
	
	private void resetCheckpoints() {
		try {
			if( redisTemplate != null ) {
				redisTemplate.boundValueOps(applicationName).getOperations().delete(applicationName);
			}
		}
		catch(Exception ex) {
			log.error("Redis cannot be located. Is it down?");
		}
	}

}
