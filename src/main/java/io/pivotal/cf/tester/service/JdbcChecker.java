package io.pivotal.cf.tester.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class JdbcChecker {
	public static Logger log = LoggerFactory.getLogger(JdbcChecker.class);

	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private StateService stateService;

	
	/**
	 * 
	 */
	@Scheduled(fixedRateString="${database.availability.rate:20000}")
	public void checkDatabaseAvailability() {
		log.info("Checking database availability...");
		if(jdbcTemplate == null) {
			log.warn("Database Service monitoring is unavailable because jdbcTemplate is null.");
			stateService.setDatabaseDown();
		}
		
		try {
			jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS test (id INT, car VARCHAR(50))");
			jdbcTemplate.execute("INSERT INTO test (id, car) VALUES (1, 'Mercedes')");
			jdbcTemplate.execute("SELECT id, car from test");
			jdbcTemplate.execute("DROP TABLE IF EXISTS test");
			stateService.setDatabaseUp();
		}
		catch(Exception ex) {
			log.warn("Database Service unavailable", ex);
			
			stateService.setDatabaseDown();
		}
	}

}
