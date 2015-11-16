package demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import io.pivotal.cf.tester.MainApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MainApplication.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class ApplicationTests {
	private static Logger log = LoggerFactory.getLogger(ApplicationTests.class);
	
	@Test
	public void contextLoads() {
		log.info("TEST");
	}

}
