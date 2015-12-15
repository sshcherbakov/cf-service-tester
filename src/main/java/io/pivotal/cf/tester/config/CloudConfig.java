package io.pivotal.cf.tester.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Configuration
@Profile("cloud")
public class CloudConfig extends AbstractCloudConfig {
	private static Logger log = LoggerFactory.getLogger(AbstractCloudConfig.class);

	@Value("${application.name:testQueue}")
	private String rabbitQueueName;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		try {
			return connectionFactory().redisConnectionFactory();
		}
		catch(Exception ex) {
			log.warn("Cannot create redisConnectionFactory. Is Redis service binding missing?", ex);
			return new JedisConnectionFactory();
		}
	}

	@Bean
	public ConnectionFactory rabbitConnectionFactory() {
		try {
			return connectionFactory().rabbitConnectionFactory();
		}
		catch(Exception ex) {
			log.warn("Cannot create rabbitConnectionFactory. Is RabbitMQ service binding missing?", ex);
			return null;
		}
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