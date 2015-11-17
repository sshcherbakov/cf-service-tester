package io.pivotal.cf.tester.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.pivotal.cf.tester.util.UtilBean;

@Component
public class ConsistencyChecker {
	private static Logger log = LoggerFactory.getLogger(ConsistencyChecker.class);

	@Autowired
	private UtilBean utils;
	
	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;

	@Value("${time.since:3000}")
	private long timeSince = 3000;
	
	@Value("${rabbit.publishers:1}")
	private int numPublishers = 1;
	
	@Value("${rabbit.consumer.instances:1}")
	private int numConsumers = 1;
	

	@Autowired
	private StateService stateService;

	private AtomicInteger instanceIdCounter = new AtomicInteger(0);
	private ThreadLocal<Integer> instanceIndex = ThreadLocal.withInitial(new Supplier<Integer>() {
		@Override
		public Integer get() {
			return instanceIdCounter.getAndIncrement();
		}
	});

	@PostConstruct
	void init() {
		try {
			for(int i=0; i<numPublishers; i++) {
				redisTemplate.delete(utils.getPublishedKey(i));
				redisTemplate.delete(utils.getPublishedZKey(i));
			}
		}
		catch(NonTransientDataAccessException ex) {
			log.error("Redis is not available. Is it down?");
		}
	}
	
	/**
	 * Save the message id and the timestamp when it has been 
	 * published as a score to the Redis ZSET 
	 * 
	 * @param messageId
	 */
	@HystrixCommand(fallbackMethod="saveToRedisFallback")
	public void saveToRedis(long messageId) {
		if(redisTemplate == null) {
			log.debug("Redis Service unavailable");
			stateService.setRedisDown();
			return;
		}
			
		long time = new Date().getTime();
		redisTemplate.boundZSetOps( utils.getPublishedZKey(instanceIndex.get()) )
			.add(messageId, time);
		
		redisTemplate.boundSetOps( utils.getPublishedKey(instanceIndex.get()) )
			.add(messageId);
	
		stateService.setRedisUp();
			
	}
	
	public void saveToRedisFallback(long messageId) {
		log.warn("Saving of [{}] to Redis has failed", messageId);
		stateService.setRedisDown();
	}
	
	public int getIndex() {
		return instanceIndex.get();
	}

	
	/**
	 * Finds message IDs in the Redis store for which no response from consumers
	 * has arrived since the configured timeout value
	 */
	@Scheduled(fixedRateString="${consistemncy.checker.rate:1000}")
	public void checkDeliveredMessages() {
		if( redisTemplate == null ) {
			return;
		}
		
		try {
			for(int i=0; i<numPublishers; i++) {
	
				Date checkTime = new Date();
				long checkTimeLong = checkTime.getTime();
				long checkSince = checkTimeLong - timeSince;
	
				BoundZSetOperations<String, Long> publishedZSetOps = redisTemplate.boundZSetOps(utils.getPublishedZKey(i));
				BoundSetOperations<String, Long> publishedSetOps = redisTemplate.boundSetOps(utils.getPublishedKey(i));
	
				// Get the ids of the messages published longer than timeout to 
				// wait for their reception
				Set<Long> oldPublishedIds = publishedZSetOps.rangeByScore(0, checkSince);
				Set<Long> oldUnrespondedIds = new HashSet<>( oldPublishedIds );
	
				for(int j=0; j<numConsumers; j++) {
									
					log.debug("Checking messages published by {} at {} {} ({}) since ({})", 
							utils.getPublishedKey(i), utils.getReceivedKey(j), checkTime, checkTimeLong, checkSince);
			
					
					BoundSetOperations<String, Long> receivedSetOps = redisTemplate.boundSetOps(utils.getReceivedKey(j));
							
					// Get the Set difference between all published ID minus all responded ids
					Set<Long> unresponded = publishedSetOps.diff( utils.getReceivedKey(j) );
					
					// Filter out recent IDs for which the timeout hasn't fired yet
					oldUnrespondedIds.retainAll(unresponded);
					
					if( !oldUnrespondedIds.isEmpty() ) {
						log.error("NO RESPONSE in {} FOR {} MESSAGES: {}", 
								utils.getReceivedKey(j), utils.getPublishedKey(i), oldPublishedIds);
					}
					
					// Clean old checked records
					receivedSetOps.remove(oldPublishedIds);
					
				}
	
				publishedZSetOps.removeRangeByScore(0, checkSince);
				publishedSetOps.remove(oldPublishedIds);
			}
		}
		catch(Exception ex) {
			log.warn("Consistency could not be checked: {}", ex.getMessage());
		}
		
	}
	
}
