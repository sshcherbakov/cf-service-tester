package io.pivotal.cf.tester.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import io.pivotal.cf.tester.service.TestMessageConsumer;
import io.pivotal.cf.tester.util.Util;

@Profile("!test")
@Component
public class ListenerContainerBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	private final String	applicationInstanceId;
	private final int 		rabbitConsumerInstances;
	private final int 		rabbitConcurrentConsumers;
	private final boolean 	rabbitAutoDeclare;
	private final boolean 	rabbitDurable;
	private final boolean 	rabbitExclusive;
	private final boolean 	rabbitAutoDelete;
	private final String 	rabbitExchangeName;
	private final String 	rabbitQueueName;
	
	public ListenerContainerBeanFactoryPostProcessor() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));
		
		this.applicationInstanceId 		= Util.getAppProperty(props, "vcap.application.instance_id", "cf-tester");
		this.rabbitConsumerInstances 	= Util.getAppPropertyInt(props, "rabbit.consumer.instances", "1");
		this.rabbitConcurrentConsumers 	= Util.getAppPropertyInt(props, "rabbit.concurrent.consumers", "1");
		this.rabbitAutoDeclare 			= Util.getAppPropertyBool(props, "rabbit.autodeclare", "true");
		this.rabbitDurable 				= Util.getAppPropertyBool(props, "rabbit.durable", "true");
		this.rabbitExclusive 			= Util.getAppPropertyBool(props, "rabbit.exclusive", "false");
		this.rabbitAutoDelete			= Util.getAppPropertyBool(props, "rabbit.autodelete", "false");
		this.rabbitExchangeName			= Util.getAppProperty(props, "rabbit.exchangeName", "testExchange");
		this.rabbitQueueName 			= Util.getAppProperty(props, "rabbit.queueName", "testQueue");
	}
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
    	
    	BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
    	for(int i=0; i < rabbitConsumerInstances; i++) {

    		String indexedQueueName = rabbitQueueName + "." + applicationInstanceId + "." + i;
    		
			registry.registerBeanDefinition("testMessageHandler" + i, 
    				BeanDefinitionBuilder.genericBeanDefinition(TestMessageConsumer.class)
    					.addConstructorArgValue(i)
    					.getBeanDefinition());
    		
			registry.registerBeanDefinition("testMessageListenerAdapter" + i, 
					BeanDefinitionBuilder.genericBeanDefinition(MessageListenerAdapter.class)
					.addConstructorArgReference("testMessageHandler" + i)
					.getBeanDefinition());
			
			registry.registerBeanDefinition("rabbitQueue" + i, 
					BeanDefinitionBuilder.genericBeanDefinition(Queue.class)
					.addConstructorArgValue(indexedQueueName)
					.addConstructorArgValue(rabbitDurable)
					.addConstructorArgValue(rabbitExclusive)
					.addConstructorArgValue(rabbitAutoDelete)
					.getBeanDefinition());
			
    		registry.registerBeanDefinition("rabbitQueueBinding" + i, 
    				BeanDefinitionBuilder.genericBeanDefinition(Binding.class)
    				.addConstructorArgValue(indexedQueueName)
    				.addConstructorArgValue(DestinationType.QUEUE)
    				.addConstructorArgValue(rabbitExchangeName)
    				.addConstructorArgValue(indexedQueueName)
    				.addConstructorArgValue(Collections.emptyMap())
    				.getBeanDefinition());
    		
    		registry.registerBeanDefinition("listenerContainer" + i, 
    				BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class)
    					.addPropertyReference("connectionFactory", "rabbitConnectionFactory")
    					.addPropertyValue("queueNames", indexedQueueName)
    					.addPropertyReference("messageListener", "testMessageListenerAdapter" + i)
    					.addPropertyReference("taskExecutor", "consumerTaskExecutor")
    					.addPropertyReference("errorHandler", "testErrorHandler")
    					.addPropertyValue("concurrentConsumers", rabbitConcurrentConsumers)
    					.addPropertyValue("autoDeclare", rabbitAutoDeclare)
    					.getBeanDefinition());
    	}
    }

}