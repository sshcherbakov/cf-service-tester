package io.pivotal.cf.tester.service;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.codahale.metrics.annotation.Timed;

import io.pivotal.cf.tester.util.Util;
import io.pivotal.cf.tester.util.UtilBean;

public class TestMessageConsumer implements MessageListener {
	private static Logger log = LoggerFactory.getLogger(TestMessageConsumer.class);

	
	@Autowired
	private UtilBean utils;

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;

	@Value("${rabbit.consumeRate:1000}")
	private int consumeRate;

	private final int instanceIndex;

	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;
	
	@Autowired
	private StateService stateService;
	
	public TestMessageConsumer(int id) {
		this.instanceIndex = id;
	}
	
	@PostConstruct
	void init() {
		if(redisTemplate != null) {
			try {
				redisTemplate.delete(utils.getReceivedKey(instanceIndex));
			}
			catch(Exception ex) {
				log.warn("Redis in unavailable");
			}
		}
	}

	@Timed
	@Override
	public void onMessage(Message message) {
		String appId = "";
		long messageId = -1;
		long msgTime = -1;
		try {
			appId = message.getMessageProperties().getAppId();
			String messageIdStr = message.getMessageProperties().getMessageId();
			messageId = Long.parseLong( messageIdStr );
			Date timestamp = message.getMessageProperties().getTimestamp();
			if(timestamp != null) {
				msgTime = timestamp.getTime();
			}
			else {
				log.error("Unexpected message {}", message);
				return;
			}
		}
		catch(NumberFormatException nfe) {
			log.error("Unexpected message {}", message);
			return;
		}
		
		log.info("({}) RCV from:[{}] id:[{}] {}", 
				utils.getReceivedKey(instanceIndex), 
				messageId, appId,
				Util.DTF.print(msgTime));
		
		if(redisTemplate == null) {
			log.debug("Redis Service unavailable");
			stateService.setRedisDown();
			return;
		}
		
		try {
			if( checkForDups(messageId) ) {
				log.warn("({}) RCV DUP id:[{}] {}", utils.getReceivedKey(instanceIndex),	messageId,	Util.DTF.print(msgTime));
			}
			saveToRedis(messageId);
			
			stateService.setRedisUp();
			
			Thread.sleep(consumeRate);
		}
		catch(Exception ex) {
			log.debug("Checkpoint in Redis failed", ex);
			
			stateService.setRedisDown();
		}
	}
	
	/**
	 * Checks for the duplicate messages by verifying the ZSet for the responded messages
	 * @param messageId
	 * @return true if the set already contains the messageId and thus the duplicate is detected
	 */
	private boolean checkForDups( final long messageId ) {
		return redisTemplate.boundSetOps( utils.getReceivedKey(instanceIndex) )
			.isMember(messageId);
	}
	
	/**
	 * Checks for the duplicate messages by setting the bit in a bit-array at the messageId position
	 * @param messageId
	 * @return true if the bit had already been set before and thus the duplicate is detected
	 */
	@SuppressWarnings("unused")
	private boolean checkForDupsByBits( final long messageId ) {
		return redisTemplate.execute(
			new RedisCallback< Boolean >() {
				@SuppressWarnings("unchecked")
				@Override
				public Boolean doInRedis( RedisConnection connection ) throws DataAccessException {
					return connection.setBit( ( ( RedisSerializer< String > )redisTemplate.getKeySerializer() )
							.serialize( utils.getReceivedKey(instanceIndex) ), messageId, true );
				}
			}
		);
	}

	/**
	 * Save the message id in the Redis SET 
	 * 
	 * @param messageId
	 */
	private void saveToRedis(long messageId) {
		
		redisTemplate.boundSetOps( utils.getReceivedKey(instanceIndex) )
			.add(messageId);
		
	}

}
