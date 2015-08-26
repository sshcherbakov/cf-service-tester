package io.pivotal.cf.tester.config;

import java.util.Collections;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
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
	
	@Value("${rabbit.autodeclare:true}")
	private boolean isRabbitAutoDeclare = true;
	
	@Value("${rabbit.concurrent.consumers:1}")
	private int rabbitConcurrentConsumers = 1;
	
	@Autowired
	public MessageListener testMessageHandler;

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
	public Queue testQueue() {
		return new Queue(rabbitQueueName, isRabbitDurable, isRabbitExclusive, isRabbitAutoDelete);
	}
	
	@Bean
	public Binding testBinding() {
		return new Binding(rabbitQueueName, DestinationType.QUEUE, rabbitExchangeName, rabbitQueueName, Collections.emptyMap());
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
	
	@Bean
	public SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory) {
		if( connectionFactory == null ) {
			return null;
		}
	    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
	    container.setConnectionFactory(connectionFactory);
	    container.setQueueNames(rabbitQueueName);
	    container.setMessageListener(new MessageListenerAdapter(testMessageHandler));
	    container.setErrorHandler(testErrorHandler);
	    container.setConcurrentConsumers(rabbitConcurrentConsumers);
	    container.setAutoDeclare(isRabbitAutoDeclare);
	    return container;
	}

}