package io.pivotal.cf.tester.util;

import org.springframework.beans.factory.annotation.Value;

public class UtilBean {

	@Value("${vcap.application.name:cf-tester}")
	private String applicationName;
	
	@Value("${vcap.application.instance_id:cf-tester}")
	private String instanceId;
	
	public String getPublishedZKey(int instanceIndex) {
		return getKeyPrefix("zpublished", instanceIndex);
	}

	public String getPublishedKey(int instanceIndex) {
		return getKeyPrefix("published", instanceIndex);
	}
	
	public String getReceivedKey(int instanceIndex) {
		return getKeyPrefix("received", instanceIndex);
	}

	public String getKeyPrefix(String prefix, int instanceIndex) {
		return new StringBuilder(applicationName)
				.append(".")
				.append(instanceId)
				.append(".")
				.append(prefix)
				.append(".")
				.append(instanceIndex)
				.toString();
	}
	
}
