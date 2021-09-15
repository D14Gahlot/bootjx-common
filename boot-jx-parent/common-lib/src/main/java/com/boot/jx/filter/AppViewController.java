package com.boot.jx.filter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class AppViewController {

    @RequestMapping(value = { "/swagger-ui.html" }, method = { RequestMethod.GET })
    public String swagger(Model model) {
	return "swagger-ui";
    }
}
