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

	@Bean
	public JedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory res = new JedisConnectionFactory();
		res.setUsePool(true);
		return res;
	}

	@Value("${rabbit.hostname:localhost}")
	private String rabbitHostname;

	@Value("${rabbit.port:5672}")
	private int rabbitPort;
	
	@Value("${rabbit.username:guest}")
	private String rabbitUsername;

	@Value("${rabbit.password:guest}")
	private String rabbitPassword;
		
	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setHost(rabbitHostname);
		connectionFactory.setPort(rabbitPort);
		connectionFactory.setUsername(rabbitUsername);
	    connectionFactory.setPassword(rabbitPassword);
	    return connectionFactory;
	}
		
}