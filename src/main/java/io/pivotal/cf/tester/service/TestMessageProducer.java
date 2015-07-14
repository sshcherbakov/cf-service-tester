package io.pivotal.cf.tester.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;

public class TestMessageProducer implements InitializingBean {
		
	@Autowired
	private TestMessagePublisher publisher;
	
	@Autowired
	@Qualifier("producer")
	private TaskExecutor taskExecutor;
	
	@Value("${rabbit.publishRate:1000}")
	private int publishRate;

	@Value("${rabbit.publishers:1}")
	private int publishers;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		for(int i=0; i<publishers; i++) {
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					while(!Thread.interrupted()) {
						publisher.publish();
						try {
							Thread.sleep(publishRate);
						} 
						catch (InterruptedException e) {
							break;
						}
					}				
				}
			});
			
		}
	}
	
}
