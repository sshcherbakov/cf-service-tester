package io.pivotal.cf.tester.service;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.pivotal.cf.tester.util.UtilBean;

public class MqttTestMessageConsumer implements MqttCallback {
	public static Logger log = LoggerFactory.getLogger(AmqpTestMessageConsumer.class);

	@Autowired
	private UtilBean utils;

	
	@Override
	public void connectionLost(Throwable cause) {
		log.info("Connection lost", cause);
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		log.info("({}) RCV MQTT from:[{}] id:[{}] at {} -> {}", 
				utils.getReceivedKey(0), 
				new String(message.getPayload()));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		log.debug("Delivery complete {}", token);		
	}

}
