package io.pivotal.cf.tester.web;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

//@RestController
@Controller
public class StartController {
	
	@Autowired(required=false)
	private ConnectionFactory rabbitConnectionFactory;
	
	@Autowired(required=false)
	private RedisConnectionFactory redisConnectionFactory;
	
	@RequestMapping("/")
    public String start(Model model) {
        model.addAttribute("statusRabbit", rabbitConnectionFactory != null);
        model.addAttribute("statusRedis", redisConnectionFactory != null);
        return "start";
    }
	
/*	
	@Autowired
	private RedisTemplate<String, Map<String,Object>> redisTemplate;

	@RequestMapping(value="/olhc/{ticker}", method=RequestMethod.GET)
	@ResponseBody
    public Map<String,Object> getOlhc(@PathVariable String ticker) {
		return redisTemplate.boundValueOps(ticker).get();
    }
	
	@RequestMapping(value="/olhc/{ticker}", method=RequestMethod.PUT)
	public void createOlhc(@PathVariable("ticker") String ticker,
			@RequestBody Config olhc) {
		
		Map<String, Object> molhc = new HashMap<String,Object>();
		molhc.put("ticker", ticker);
		molhc.put("time", new Date());
		molhc.put("open", olhc.getOpen());
		molhc.put("low", olhc.getLow());
		molhc.put("high", olhc.getHigh());
		molhc.put("close", olhc.getClose());
		redisTemplate.boundValueOps(ticker).set(molhc);
	}
*/	
}
