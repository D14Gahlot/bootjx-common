package com.boot.jx.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.boot.jx.AppConfig;

@Controller
public class AppViewController {

    @Autowired
    AppConfig appConfig;

    @RequestMapping(value = { "/swagger-ui.html" }, method = { RequestMethod.GET })
    public String swagger(Model model) {
	return "swagger-ui";
    }

    @RequestMapping(value = { "/swagger-uix.html" }, method = { RequestMethod.GET })
    public String swagger2(Model model) {
	model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
	return "swagger-uix";
    }
}
