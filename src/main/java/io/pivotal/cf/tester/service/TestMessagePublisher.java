package io.pivotal.cf.tester.service;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
	
	@Autowired(required=false)
	private RabbitTemplate rabbitTemplate;

	@Autowired(required=false)
	private RedisTemplate< String, Long > redisTemplate;

	@Autowired
	private StateService stateService;

	@PostConstruct
	void init() {
		
		rabbitTemplate.setConfirmCallback(new ConfirmCallback() {
			@Override
			public void confirm(CorrelationData correlationData, boolean ack, String cause) {
				log.debug("id={} ack={} cause={}", correlationData.getId(), ack, cause);
			}
		});
	}
	
	@Timed
	public void publish() {
		
		if(rabbitTemplate == null) {
			log.debug("RabbitMQ Service unavailable");
			stateService.setRabbitDown();
			return;
		}
		
		final Date now = new Date();
		String timeString = Util.DTF.print(now.getTime());
		
		String messageBody = new StringBuilder()
			.append(" (").append(utils.getKeyPrefix()).append(")")
			.append(" PUB ")
			.append(timeString)
			.toString();
		
		long nextId = stateService.getNextId();
		Message message = MessageBuilder
				.withBody(messageBody.getBytes())
				.setMessageId( Long.toString(nextId) )
				.setTimestamp(now)
				.build();
		
		sendToRabbit(messageBody, message);
		saveToRedis(nextId);
	}

	private void sendToRabbit(String messageBody, final Message message) {
		try {
			rabbitTemplate.send(rabbitExchangeName, rabbitQueueName, 
					message, new CorrelationData(message.getMessageProperties().getMessageId()));
			log.debug(messageBody);
		}
		catch(Exception ex) {
			log.debug("Publish to Rabbit failed", ex);
			
			stateService.setRabbitDown();
		}
	}
	
	/**
	 * Save the message id and the timestamp when it has been 
	 * published as a score to the Redis ZSET 
	 * 
	 * @param id
	 */
	private void saveToRedis(long id) {
		
		long time = new Date().getTime();
		redisTemplate.boundZSetOps( utils.getPublishedKey() )
			.add(id, time);
		
	}

}
