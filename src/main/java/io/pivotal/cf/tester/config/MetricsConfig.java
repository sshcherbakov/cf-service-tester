package io.pivotal.cf.tester.config;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Slf4jReporter.LoggingLevel;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics
public class MetricsConfig extends MetricsConfigurerAdapter {
	private static Logger logger = LoggerFactory.getLogger(MetricsConfig.class);

	@Override
	public void configureReporters(MetricRegistry metricRegistry) {
		Slf4jReporter
			.forRegistry(metricRegistry)
			.outputTo(logger)
			.withLoggingLevel(LoggingLevel.INFO)
			.build()
			.start(10, TimeUnit.SECONDS);
	}

}
