package io.pivotal.cf.tester.service;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;

import io.pivotal.cf.tester.config.AppConfig;

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
@Profile("!" + AppConfig.PROFILE_MQTT)
@Component
public class AmqpTestMessagePublisher extends AbstractTestMessagePublisher {
	private static Logger log = LoggerFactory.getLogger(AmqpTestMessagePublisher.class);
	
	
	@Autowired(required=false)
	private RabbitTemplate rabbitTemplate;

	
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
	
	
	@Override
	@Timed
	public void publish() {
		if(rabbitTemplate == null) {
			log.debug("RabbitMQ Service unavailable");
			stateService.setRabbitDown();
			return;
		}
		
		Date now = new Date();
		String messageId = getMessageId();
		String messagePayload = getMessageBody(messageId, now);
		
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setAppId(instanceName);
		messageProperties.setMessageId(messageId);
		messageProperties.setTimestamp(now);
		
		Message amqpMsg = MessageBuilder
					.withBody(messagePayload.getBytes())
					.andProperties(messageProperties)
					.build();
		
		try {
			
			rabbitTemplate.send(rabbitExchangeName, rabbitQueueName, 
					amqpMsg, new CorrelationData(messageId));
			
			log.info("{} [{}] {}", 
					instanceName, 
					amqpMsg.getMessageProperties().getMessageId(),
					messagePayload);
			
			stateService.setRabbitUp();
			
		}
		catch(AmqpException ex) {
			log.warn("({}) Publish of [{}] to RabbitMQ has failed",
					utils.getPublishedKey(consistencyChecker.getIndex()), messageId);
			
			stateService.setRabbitDown();
		}
		
	}
	
}
