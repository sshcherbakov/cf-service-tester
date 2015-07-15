package io.pivotal.cf.tester.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ErrorHandler;

public class TestErrorHandler implements ErrorHandler {
	private static Logger log = LoggerFactory.getLogger(TestErrorHandler.class);
	
	@Autowired
	private StateService stateService;

	@Override
	public void handleError(Throwable t) {
		log.debug("RabbitMQ receive error", t);
		
		stateService.setRabbitDown();
	}

}
