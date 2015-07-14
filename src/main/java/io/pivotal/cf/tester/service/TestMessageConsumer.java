package io.pivotal.cf.tester.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Value;

import com.codahale.metrics.annotation.Timed;

public class TestMessageConsumer implements MessageListener {
	private static Logger log = LoggerFactory.getLogger(TestMessageConsumer.class);

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;
	
	@Value("${vcap.application.instance_index:0}")
	private int instanceIndex;

	@Timed
	@Override
	public void onMessage(Message message) {
		log.debug("{} [{}] ({}) RCV {}", 
				applicationName, instanceId, instanceIndex,
				Util.DTF.print(message.getMessageProperties().getTimestamp().getTime()));
	}
	
}
