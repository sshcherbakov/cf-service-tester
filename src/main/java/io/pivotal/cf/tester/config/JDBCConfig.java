package io.pivotal.cf.tester.config;


import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class JDBCConfig {
	
	
	@Bean
	public JdbcTemplate jdbcTemplate (DataSource dataSource) {
		if(null == dataSource ) {
			return null;
		}
		return new JdbcTemplate(dataSource);
	}

	
}