package io.pivotal.cf.tester.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.codahale.metrics.annotation.Timed;

import io.pivotal.cf.tester.util.Util;
import io.pivotal.cf.tester.util.UtilBean;

public class AmqpTestMessageConsumer implements MessageListener {
	public static Logger log = LoggerFactory.getLogger(AmqpTestMessageConsumer.class);

	@Autowired
	private DuplicatesChecker dupChecker;
	
	@Autowired
	private UtilBean utils;

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;

	@Value("${rabbit.consumeRate:0}")
	private int consumeRate;

	private final int instanceIndex;

	
	public AmqpTestMessageConsumer(int id) {
		this.instanceIndex = id;
	}
	
	
	@Timed
	@Override
	public void onMessage(Message message) {
		
		String appId = getAppId(message);
		Long messageId = getMessageId(message);
		String msgTime = getMessageTime(message);
		
		
		if( dupChecker.checkForDups(instanceIndex, messageId) ) {
			log.warn("({}) DUP from:[{}] id:[{}] {}",
					utils.getReceivedKey(instanceIndex),	
					appId, messageId, msgTime);
		}
		else {
			log.info("({}) RCV from:[{}] id:[{}] at {} -> {}", 
					utils.getReceivedKey(instanceIndex), 
					appId, messageId, 
					msgTime, new String(message.getBody()));
		}
					
		Util.sleep(consumeRate);
		
	}

	
	private String getAppId(Message message) {
		return message.getMessageProperties().getAppId();
	}
	
	private Long getMessageId(Message message) {
		try {
			String messageIdStr = message.getMessageProperties().getMessageId();
			return Long.parseLong( messageIdStr );
		}
		catch(NumberFormatException nfe) {
			log.debug("Unexpected message {}", message);
			return null;
		}
	}
	
	private String getMessageTime(Message message) {
		try {
			Date timestamp = message.getMessageProperties().getTimestamp();
			if(timestamp != null) {
				return Util.DTF.print(timestamp.getTime());
			}
		}
		catch(NumberFormatException nfe) {
			log.debug("Unexpected message {}", message);
		}
		
		log.debug("Unexpected message {}", message);
		return null;
	}

}
