package io.pivotal.cf.tester.config;

import io.pivotal.cf.tester.service.TestMessageHandler;
import io.pivotal.cf.tester.service.TestMessageProducer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class AppConfig {

	@Bean
	public TestMessageHandler testMessageHandler() {
		return new TestMessageHandler();
	}
	
	@Bean
	public TestMessageProducer testMessageProducer() {
		return new TestMessageProducer();
	}

}