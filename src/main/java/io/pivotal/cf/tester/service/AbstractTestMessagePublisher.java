package io.pivotal.cf.tester.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import io.pivotal.cf.tester.util.Util;
import io.pivotal.cf.tester.util.UtilBean;


public abstract class AbstractTestMessagePublisher implements TestMessagePublisher {
	
	@Value("${vcap.application.instance_id:default}")
	protected String instanceName;

	@Autowired
	protected UtilBean utils;
	
	@Value("${rabbit.exchangeName:testExchange}")
	protected String rabbitExchangeName;

	@Value("${rabbit.queueName:testQueue}")
	protected String rabbitQueueName;
	
	@Value("${rabbit.publishers:2}")
	protected int numPublishers;
	
	@Autowired
	protected ConsistencyChecker consistencyChecker;

	@Autowired
	protected StateService stateService;
	
		
	protected String getMessageBody(final Date now) {
		String timeString = Util.DTF.print(now.getTime());
		
		String messageBody = new StringBuilder()
			.append(" (").append(utils.getPublishedKey(consistencyChecker.getIndex())).append(")")
			.append(" PUB ")
			.append(timeString)
			.toString();
		return messageBody;
	}

	protected String getMessageId() {
		long nextId = stateService.getNextId();
		return Long.toString(nextId);
	}
	
}
