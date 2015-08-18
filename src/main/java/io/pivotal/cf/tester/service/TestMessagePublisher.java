package io.pivotal.cf.tester.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.codahale.metrics.annotation.Timed;

public class TestMessagePublisher {
	private static Logger log = LoggerFactory.getLogger(TestMessagePublisher.class);
	
	
	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;
	
	@Value("${vcap.application.instance_index:0}")
	private int instanceIndex;
	
	@Value("${rabbit.queueName:testQueue}")
	private String rabbitQueueName;

	@Autowired(required=false)
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private StateService stateService;
	
	@Timed
	public void publish() {
		
		if(rabbitTemplate == null) {
			log.debug("RabbitMQ Service unavailable");
			stateService.setRabbitDown();
			return;
		}
		
		final Date now = new Date();
		String timeString = Util.DTF.print(now.getTime());
		
		String messageBody = new StringBuilder(applicationName)
			.append(" [").append(instanceId).append("]")
			.append(" (").append(instanceIndex).append(")")
			.append(" PUB ")
			.append(timeString)
			.toString();
		
		Message message = MessageBuilder
				.withBody(messageBody.getBytes())
				.setMessageId(stateService.getNextId())
				.setTimestamp(now)
				.build();
		
		sendToRabbit(messageBody, message);
		
	}

	private void sendToRabbit(String messageBody, Message message) {
		try {
			rabbitTemplate.send(rabbitQueueName, message);
			log.debug(messageBody);
		}
		catch(Exception ex) {
			log.debug("Publish to Rabbit failed", ex);
			
			stateService.setRabbitDown();
		}
	}
	
}
