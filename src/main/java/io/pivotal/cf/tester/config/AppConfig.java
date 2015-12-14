package io.pivotal.cf.tester.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.pivotal.cf.tester.util.UtilBean;

@EnableHystrix
@EnableScheduling
@Configuration
public class AppConfig {
	
	public final static String PROFILE_PRODUCER = "producer";
	public final static String PROFILE_CONSUMER = "consumer";
	public final static String PROFILE_HEADLESS = "headless";
	public final static String PROFILE_MQTT = "mqtt";
	
	@Value("${rabbit.publishers:1}")
	private int publishers;
	
	@Value("${rabbit.consumer.instances:1}")
	private int rabbitConsumerInstances;
	
	@Value("${rabbit.concurrent.consumers:1}")
	private int rabbitConcurrentConsumers;
	
	@Bean
	public UtilBean utilBean() {
		return new UtilBean();
	}
	
	@Bean
	@Qualifier("producer")
	public ThreadPoolTaskExecutor publisherTaskExecutor() {
		if(publishers <= 0) {
			return null;
		}
		
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(publishers);
		taskExecutor.setMaxPoolSize(publishers);
		taskExecutor.setQueueCapacity(0);
		taskExecutor.setThreadNamePrefix("publisher-");
		return taskExecutor;
	}
	
	@Bean
	@Qualifier("consumer")
	public ThreadPoolTaskExecutor consumerTaskExecutor() {
		if(rabbitConsumerInstances <= 0) {
			return null;
		}
		
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(rabbitConsumerInstances * rabbitConcurrentConsumers);
		taskExecutor.setMaxPoolSize(rabbitConsumerInstances * rabbitConcurrentConsumers);
		taskExecutor.setThreadNamePrefix("consumer-");
		return taskExecutor;
	}
	
}