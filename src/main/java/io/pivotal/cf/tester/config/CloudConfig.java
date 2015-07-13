package io.pivotal.cf.tester.config;

import java.util.Properties;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {

	@Value("${application.name:testQueue}")
	private String rabbitQueueName;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return connectionFactory().redisConnectionFactory();
	}

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		return connectionFactory().rabbitConnectionFactory();
	}
	
	@Bean
    public Properties cloudProperties() {
        return properties();
    }
		
	@Bean
	public Queue testQueue() {
	    return new Queue(rabbitQueueName);
	}

}