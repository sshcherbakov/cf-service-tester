package io.pivotal.cf.tester.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("mqtt")
public class MqttConfig implements SmartLifecycle {
	private static Logger log = LoggerFactory.getLogger(MqttConfig.class);

	@Value("${mqtt.serverURIs}")
	private String serverURIs = "tcp://localhost:1883";
	
	
	@Value("${vcap.application.name:cf-tester}")
	private String appName;

	@Value("${vcap.application.instance_id:one}")
	private String instanceId;
	
	@Value("${spring.rabbitmq.virtualHost:/}")
	private String mqttVhost;
	
	@Value("${rabbit.username:guest}")
	private String mqttUsername;
	
	@Value("${rabbit.password:guest}")
	private String mqttPassword;

	
	@Bean
	public MqttClient mqttClient() throws MqttException {
		
		String suri = serverURIs.split(",")[0];
		
		return new MqttClient(suri, 
				appName + "-" + instanceId);
	}
	
	@Bean
	public MqttConnectOptions mqttConnectOptions() {
		
		String[] suris = serverURIs.split(",");
		
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setServerURIs(suris);
		connOpts.setUserName(mqttVhost + ":" + mqttUsername);
		connOpts.setPassword(mqttPassword.toCharArray());
		
		return connOpts;
	}
	
	private boolean isRunning = false;
	
	@Override
	public void start() {
		try {
			mqttClient().connect(mqttConnectOptions());
			isRunning = true;
		} 
		catch (MqttException e) {
			log.error("Cannot start the MQTT client", e);
		}
	}

	@Override
	public void stop() {
		try {
			MqttClient mqttClient = mqttClient();
			if(mqttClient.isConnected()) {
				mqttClient.disconnect();
			}
			isRunning = false;
		} 
		catch (MqttException e) {
			log.error("Error disconnecting the MQTT client", e);
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}
	
}
