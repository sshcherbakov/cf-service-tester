package io.pivotal.cf.tester.util;

import org.springframework.beans.factory.annotation.Value;

public class UtilBean {

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;
	
	@Value("${vcap.application.instance_index:0}")
	private int instanceIndex;

	public String getPublishedZKey() {
		return getKeyPrefix() + ".zpublished";
	}

	public String getPublishedKey() {
		return getKeyPrefix() + ".published";
	}
	
	public String getReceivedKey() {
		return getKeyPrefix() + ".received";
	}

	public String getKeyPrefix() {
		return new StringBuilder(applicationName)
				.append(".")
				.append(instanceId)
				.append(".")
				.append(instanceIndex)
				.toString();
	}

}
