package com.ykb.tdp.testsuite.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	
	private final Logger helloLogger = LoggerFactory.getLogger(HelloController.class);
	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public String index() {
		helloLogger.info("Hello service Started");
		return "Hello World";
	}

}
