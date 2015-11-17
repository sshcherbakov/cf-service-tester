package io.pivotal.cf.tester.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import io.pivotal.cf.tester.config.AppConfig;

@Profile(AppConfig.PROFILE_PRODUCER)
@Component
public class TestMessageProducer implements InitializingBean, SmartLifecycle {
	private static Logger log = LoggerFactory.getLogger(TestMessageProducer.class);
		
	@Autowired
	private TestMessagePublisher publisher;
	
	@Autowired
	@Qualifier("producer")
	private TaskExecutor taskExecutor;
	
	@Value("${rabbit.publishRate:1000}")
	private int publishRate;

	@Value("${rabbit.publishers:1}")
	private int publishers;
	
	private volatile boolean isRunning = true;
	
	@Override
	public void afterPropertiesSet() throws Exception {

		for(int i=0; i<publishers; i++) {
			
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					while(isRunning && !Thread.interrupted()) {
						try {
							publisher.publish();
							Thread.sleep(publishRate);
						} 
						catch (InterruptedException e) {
							break;
						}
						catch(RuntimeException rex) {
							log.error("Could not publish:", rex);
						}
					}				
				}
			});
			
		}
	}
	
	@Override
	public void start() {
		log.debug("Starting {}", this.getClass().getSimpleName());
	}

	@Override
	public void stop() {
		log.debug("Stopping {}", this.getClass().getSimpleName());
		this.isRunning = false;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		log.debug("Stopping {}", this.getClass().getSimpleName());
		this.isRunning = false;
		callback.run();
	}
	
}
