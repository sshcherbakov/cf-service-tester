package io.pivotal.cf.tester.service;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import io.pivotal.cf.tester.util.UtilBean;

@Service
public class DuplicatesChecker {
	private static Logger log = LoggerFactory.getLogger(DuplicatesChecker.class);

	@Autowired
	private UtilBean utils;
	
	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;
	
	@Value("${rabbit.consumer.instances:1}")
	private int numConsumers = 1;
	

	@Autowired
	private StateService stateService;

	
	@PostConstruct
	void init() {
		if(redisTemplate != null) {
			try {
				for(int i=0; i<numConsumers; i++) {
					redisTemplate.delete(utils.getReceivedKey(i));
				}
			}
			catch(NonTransientDataAccessException ex) {
				log.error("Redis is not available. Is it down?");
			}
		}
	}
	
	
	public void saveToRedisFallback(long messageId) {
		log.warn("Saving of [{}] to Redis has failed", messageId);
		stateService.setRedisDown();
	}
	
	
	/**
	 * Checks for the duplicate messages by verifying the Set for the responded messages
	 * and saves the message id in the Redis set of the seen messages 
	 * @param messageId
	 * @return true if the set already contains the messageId and thus the duplicate is detected
	 */
	public boolean checkForDups( final int consumerIndex, final Long messageId ) {
		if(redisTemplate == null) {
			log.debug("Redis Service unavailable");
			stateService.setRedisDown();
			return false;			// <-- amnesia if Redis is not available
		}
		
		if(messageId == null) {		// Probably an MQTT message
			log.debug("No messageId found");
			return false;
		}
		
		boolean res = false;
		try {
			res = redisTemplate
					.boundSetOps( utils.getReceivedKey(consumerIndex) )
					.add(messageId) <= 0;
						
			stateService.setRedisUp();
		}
		catch(Exception ex) {
			log.debug("Checkpoint in Redis failed", ex);
			
			stateService.setRedisDown();
		}
		
		return res;
	}

	
	/**
	 * An unused example.
	 * Checks for the duplicate messages by setting the bit in a bit-array at the messageId position
	 * @param messageId
	 * @return true if the bit had already been set before and thus the duplicate is detected
	 */
	@SuppressWarnings("unused")
	private boolean checkForDupsByBits( final int consumerIndex, final long messageId ) {
		return redisTemplate.execute(
			new RedisCallback< Boolean >() {
				@SuppressWarnings("unchecked")
				@Override
				public Boolean doInRedis( RedisConnection connection ) throws DataAccessException {
					return connection.setBit( ( ( RedisSerializer< String > )redisTemplate.getKeySerializer() )
							.serialize( utils.getReceivedKey(consumerIndex) ), messageId, true );
				}
			}
		);
	}

}
