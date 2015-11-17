package io.pivotal.cf.tester.config;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
public class RabbitConfig {

	@Value("${rabbit.queueName:testQueue}")
	private String rabbitQueueName;

	@Value("${rabbit.exchangeName:testExchange}")
	private String rabbitExchangeName;
	
	@Value("${rabbit.durable:true}")
	private boolean isRabbitDurable = true;
	
	@Value("${rabbit.exclusive:false}")
	private boolean isRabbitExclusive = false;
	
	@Value("${rabbit.autodelete:false}")
	private boolean isRabbitAutoDelete = false;
		
	@Autowired
	public ErrorHandler testErrorHandler;
	
	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		if( connectionFactory == null ) {
			return null;
		}
		RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
		rabbitAdmin.setIgnoreDeclarationExceptions(true);
		return rabbitAdmin;
	}

	@Bean
	public AbstractExchange testExchange() {
	    return new FanoutExchange(rabbitExchangeName, isRabbitDurable, isRabbitAutoDelete);
	}
	
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		if( connectionFactory == null ) {
			return new RabbitTemplate() {
				@Override
				public void afterPropertiesSet() {
				}
				
			};
		}
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setRoutingKey(rabbitQueueName);
		return rabbitTemplate;
	}

}