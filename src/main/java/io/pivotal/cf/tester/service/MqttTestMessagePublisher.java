package io.pivotal.cf.tester.service;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cf.tester.config.AppConfig;

/**
 * An instance of this class is responsible to send a test message to the 
 * configured RabbitMQ destination exchange using MQTT protocol.
 * 
 * @author sshcherbakov
 *
 */
@Profile(AppConfig.PROFILE_MQTT)
@Component
public class MqttTestMessagePublisher extends AbstractTestMessagePublisher {
	private static Logger log = LoggerFactory.getLogger(MqttTestMessagePublisher.class);
	
	
	@Autowired(required=false)
	private MqttClient mqttClient;
	
	@Value("${mqtt.qos:1}")
	protected int mqttQos = 1;

	
	@Override
	public void publish() {
		if(mqttClient == null) {
			log.debug("MQTT client unavailable");
			stateService.setRabbitDown();
			return;
		}

		Date now = new Date();
		String messageId = getMessageId();
		String messagePayload = getMessageBody(messageId, now);
		
		try {

			MqttTopic topic = mqttClient.getTopic(rabbitQueueName);
			MqttMessage mqttMessage = new MqttMessage(messagePayload.getBytes());
			mqttMessage.setQos(mqttQos);
			MqttDeliveryToken token = topic.publish(mqttMessage);
			if(mqttQos > 0) {
				token.waitForCompletion();
			}
			
			log.info("{} [{}] {}", instanceName, 
					messageId, messagePayload);
			
			stateService.setRabbitUp();
			
		}
		catch(MqttException ex) {
			log.warn("({}) Publish of MQTT messate [{}] to RabbitMQ has failed",
					utils.getPublishedKey(consistencyChecker.getIndex()), messageId);
			
			stateService.setRabbitDown();
		}
		
	}
	
}
