package io.pivotal.cf.tester.service;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;

import io.pivotal.cf.tester.config.AppConfig;
import io.pivotal.cf.tester.util.Util;
import io.pivotal.cf.tester.util.UtilBean;

/**
 * An instance of this class is responsible to send a test message to the 
 * configured RabbitMQ destination exchange.
 * The class is configured to support RabbitMQ Publisher Confirms feature.
 * 
 * When a message is successfully confirmed by the RabbitMQ broker, its ID
 * gets stored in Redis in order to enable the {@link ConsistencyChecker} to
 * verify whether the message has been lost along the way to all consumers. 
 * 
 * @author sshcherbakov
 *
 */
@Profile(AppConfig.PROFILE_PRODUCER)
@Component
public class TestMessagePublisher {
	private static Logger log = LoggerFactory.getLogger(TestMessagePublisher.class);
	
	@Value("${vcap.application.instance_id:default}")
	private String instanceName;

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

	@Autowired
	private ConsistencyChecker consistencyChecker;

	@Autowired
	private StateService stateService;

		
	@PostConstruct
	void init() {
		
		rabbitTemplate.setConfirmCallback( (correlationData, ack, cause) -> {
				log.debug("id={} ack={} cause={}", correlationData.getId(), ack, cause);
				long msgId = -1;
				if( ack ) {
					msgId = Long.parseLong(correlationData.getId());
					consistencyChecker.saveToRedis( msgId );
				}
				else {
					log.warn("Message [{}] has NOT been confirmed");
				}
		});
		
	}
	
	
	@Timed
	public void publish() {
		
		final Date now = new Date();
		String timeString = Util.DTF.print(now.getTime());
		
		String messageBody = new StringBuilder()
			.append(" (").append(utils.getPublishedKey(consistencyChecker.getIndex())).append(")")
			.append(" PUB ")
			.append(timeString)
			.toString();
		
		long nextId = stateService.getNextId();
		Message message = MessageBuilder
				.withBody(messageBody.getBytes())
				.setAppId(instanceName)
				.setMessageId( Long.toString(nextId) )
				.setTimestamp(now)
				.build();
			
		sendToRabbit(messageBody, message);
					
	}

	
	private void sendToRabbit(String messageBody, final Message message) {
		if(rabbitTemplate == null) {
			log.debug("RabbitMQ Service unavailable");
			stateService.setRabbitDown();
			return;
		}
		
		String messageId = message.getMessageProperties().getMessageId();
		try {
			
			rabbitTemplate.send(rabbitExchangeName, rabbitQueueName, 
					message, new CorrelationData(messageId));
			
			log.info("{} {}", instanceName, messageBody);
			stateService.setRabbitUp();
			
		}
		catch(AmqpException ex) {
			log.warn("({}) Publish of [{}] to RabbitMQ has failed",
					utils.getPublishedKey(consistencyChecker.getIndex()), messageId);
			
			stateService.setRabbitDown();
		}
		
	}
	
}
