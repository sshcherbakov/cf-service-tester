package io.pivotal.cf.tester.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;


@Service
public class JdbcChecker {
	public static Logger log = LoggerFactory.getLogger(JdbcChecker.class);

	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private StateService stateService;

	
	/**
	 * 
	 */
	public boolean checkDatabaseAvailability() {
		boolean isDbAvailable = false;
		if(jdbcTemplate == null) {
			log.warn("Database Service unavailable because jdbcTemplate is null.");
			stateService.setDatabaseDown();
			
			return isDbAvailable;			
		}
		
		try {
			jdbcTemplate.execute("SELECT 1");
			stateService.setDatabaseUp();
			isDbAvailable = true;
		}
		catch(Exception ex) {
			log.warn("Database Service unavailable", ex);
			
			stateService.setDatabaseDown();
		}
		
		return isDbAvailable;
	}

}
