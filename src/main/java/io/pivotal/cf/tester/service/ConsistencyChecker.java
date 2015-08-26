package io.pivotal.cf.tester.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import io.pivotal.cf.tester.util.UtilBean;

public class ConsistencyChecker {
	private static Logger log = LoggerFactory.getLogger(ConsistencyChecker.class);

	@Autowired
	private UtilBean utils;
	
	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;

	@Value("${time.since:3000}")
	private long timeSince = 3000;
	
	/**
	 * Finds message IDs in the Redis store for which no response from consumers
	 * has arrived since the configured timeout value
	 */
	@Scheduled(fixedRateString="${consistemncy.checker.rate:1000}")
	public void checkDeliveredMessages() {
		if( redisTemplate == null ) {
			return;
		}
		
		Date checkTime = new Date();
		long checkTimeLong = checkTime.getTime();
		long checkSince = checkTimeLong - timeSince;
		
		log.debug("Checking delivered messages ({}) at {} ({}) since ({})", 
				utils.getKeyPrefix(), checkTime, checkTimeLong, checkSince);

		BoundZSetOperations<String, Long> publishedZSetOps = redisTemplate.boundZSetOps(utils.getPublishedZKey());
		BoundSetOperations<String, Long> publishedSetOps = redisTemplate.boundSetOps(utils.getPublishedKey());
		BoundSetOperations<String, Long> receivedSetOps = redisTemplate.boundSetOps(utils.getReceivedKey());

		// Get the ids of the messages published longer than timeout to 
		// wait for their reception
		Set<Long> oldPublishedIds = publishedZSetOps.rangeByScore(0, checkSince);
		Set<Long> oldUnrespondedIds = new HashSet<>( oldPublishedIds );
		
		// Get the Set difference between all published ID minus all responded ids
		Set<Long> unresponded = publishedSetOps.diff( utils.getReceivedKey() );
		
		// Filter out recent IDs for which the timeout hasn't fired yet
		oldUnrespondedIds.retainAll(unresponded);
		
		if( !oldUnrespondedIds.isEmpty() ) {
			log.error("NO RESPONSE FOR MESSAGES: {}", oldPublishedIds);
		}
		
		// Clean old checked records
		publishedZSetOps.removeRangeByScore(0, checkSince);
		publishedSetOps.remove(oldPublishedIds);
		receivedSetOps.remove(oldPublishedIds);
	}

	
}
