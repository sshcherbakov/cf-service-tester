package io.pivotal.cf.tester.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.pivotal.cf.tester.service.ConsistencyChecker;
import io.pivotal.cf.tester.service.StateService;
import io.pivotal.cf.tester.service.TestErrorHandler;
import io.pivotal.cf.tester.service.TestMessageProducer;
import io.pivotal.cf.tester.service.TestMessagePublisher;
import io.pivotal.cf.tester.util.UtilBean;

@EnableScheduling
@Configuration
public class AppConfig {
	
	@Value("${rabbit.publishers:1}")
	private int publishers;
	
	@Value("${rabbit.consumer.instances:1}")
	int rabbitConsumerInstances;
	
	@Bean
	public UtilBean utilBean() {
		return new UtilBean();
	}

	@Bean
	public TestErrorHandler testErrorHandler() {
		return new TestErrorHandler();
	}
	
	@Bean
	public TestMessagePublisher testMessagePublisher() {
		return new TestMessagePublisher();
	}

	@Bean
	public TestMessageProducer testMessageProducer() {
		return new TestMessageProducer();
	}
	
	@Bean
	public ConsistencyChecker consistencyChecker() {
		return new ConsistencyChecker();
	}
	
	@Bean
	@Qualifier("producer")
	public TaskExecutor publisherTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(publishers);
		taskExecutor.setMaxPoolSize(publishers);
		taskExecutor.setQueueCapacity(0);
		taskExecutor.setThreadNamePrefix("publisher-");
		return taskExecutor;
	}

	@Bean
	public StateService stateService() {
		return new StateService();
	}
	
}