package io.pivotal.cf.tester.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;


@Configuration
@Profile("!cloud")
public class StandaloneConfig {

	@Value("${redis.hostname:localhost}")
	private String redisHostname;
	
	@Value("${redis.publisher.confirms:true}")
	private boolean publisherConfirms = true;	

	@Value("${redis.port:6379}")
	private int redisPort;

	@Value("${rabbit.connection.timeout:30000}")
	private int rabbitConnectionTimeout;
	
	@Value("${rabbit.channel.checkout.timeout:0}")
	private int rabbitChannelCheckoutTimeout = 0;
	
	@Value("${rabbit.close.timeout:3000}")
	private int rabbitCloseTimeout;
	
	@Value("${rabbit.network.recovery.interval:5000}")
	private int rabbitNetworkRecoveryInterval;
	
	@Value("${rabbit.heartbeat.interval:580}")
	private int rabbitHeartbeatInterval;
	
	@Value("${spring.rabbitmq.virtualHost:/}")
	private String rabbitVirtualHost;
	
	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory res = new JedisConnectionFactory();
		res.setUsePool(true);
		res.setHostName(redisHostname);
		res.setPort(redisPort);
		return res;
	}

	@Value("${rabbit.addresses:localhost:5672}")
	private String rabbitAddresses;
	
	@Value("${rabbit.username:guest}")
	private String rabbitUsername;

	@Value("${rabbit.password:guest}")
	private String rabbitPassword;
		
	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
		cf.setNetworkRecoveryInterval(rabbitNetworkRecoveryInterval);
		cf.setRequestedHeartbeat(rabbitHeartbeatInterval);
		
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(cf);
		connectionFactory.setVirtualHost(rabbitVirtualHost);
		connectionFactory.setAddresses(rabbitAddresses);
		connectionFactory.setUsername(rabbitUsername);
	    connectionFactory.setPassword(rabbitPassword);
	    connectionFactory.setPublisherConfirms(publisherConfirms);
	    connectionFactory.setConnectionTimeout(rabbitConnectionTimeout);
	    connectionFactory.setChannelCheckoutTimeout(rabbitChannelCheckoutTimeout);
	    connectionFactory.setCloseTimeout(rabbitCloseTimeout);
	    return connectionFactory;
	}
		
}