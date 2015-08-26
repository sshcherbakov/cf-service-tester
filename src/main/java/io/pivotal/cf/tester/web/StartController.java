package io.pivotal.cf.tester.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import io.pivotal.cf.tester.service.StateService;

@Controller
public class StartController {
	
	@Autowired
	private StateService stateService;
	
	@RequestMapping("/")
    public String start(Model model) {
        model.addAttribute("statusRabbit", stateService.isRabbitUp());
        model.addAttribute("statusRedis", stateService.isRedisUp());
        return "start";
    }
	
}
