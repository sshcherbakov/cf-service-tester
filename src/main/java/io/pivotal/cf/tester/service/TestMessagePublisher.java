package io.pivotal.cf.tester.service;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;

import com.codahale.metrics.annotation.Timed;

import io.pivotal.cf.tester.util.Util;
import io.pivotal.cf.tester.util.UtilBean;

public class TestMessagePublisher {
	private static Logger log = LoggerFactory.getLogger(TestMessagePublisher.class);
	

	@Autowired
	private UtilBean utils;
	
	@Value("${rabbit.exchangeName:testExchange}")
	private String rabbitExchangeName;

	@Value("${rabbit.queueName:testQueue}")
	private String rabbitQueueName;
	
	@Value("${rabbit.publishers:2}")
	private int numPublishers;
	
	@Autowired(required=false)
	private RabbitTemplate rabbitTemplate;

	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;

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
		
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				log.debug("id={} ack={} cause={}", correlationData.getId(), ack, cause);
			}
		});
		
		for(int i=0; i<numPublishers; i++) {
			redisTemplate.delete(utils.getPublishedKey(i));
			redisTemplate.delete(utils.getPublishedZKey(i));
		}
		
	}
	
	
	
	@Timed
	public void publish() {
				
		final Date now = new Date();
		String timeString = Util.DTF.print(now.getTime());
		
		String messageBody = new StringBuilder()
			.append(" (").append(utils.getPublishedKey(instanceIndex.get())).append(")")
			.append(" PUB ")
			.append(timeString)
			.toString();
		
		long nextId = stateService.getNextId();
		Message message = MessageBuilder
				.withBody(messageBody.getBytes())
				.setMessageId( Long.toString(nextId) )
				.setTimestamp(now)
				.build();
			
		try {
			
			sendToRabbit(messageBody, message);
			saveToRedis(nextId);
			
		}
		catch(AmqpException ex) {
			log.warn("Publish of [{}] to RabbitMQ has failed", nextId);
			stateService.setRabbitDown();
		}
		catch(DataAccessException ex) {
			log.warn("Saving of [{}] to Redis has failed", nextId);
			stateService.setRedisDown();
		}
		
	}

	
	
	private void sendToRabbit(String messageBody, final Message message) {
		if(rabbitTemplate == null) {
			log.debug("RabbitMQ Service unavailable");
			stateService.setRabbitDown();
			return;
		}

		rabbitTemplate.send(rabbitExchangeName, rabbitQueueName, 
				message, new CorrelationData(message.getMessageProperties().getMessageId()));
		
		log.debug(messageBody);
		stateService.setRabbitUp();
	}
	
	/**
	 * Save the message id and the timestamp when it has been 
	 * published as a score to the Redis ZSET 
	 * 
	 * @param messageId
	 */
	private void saveToRedis(long messageId) {
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

}
