package com.boot.jx.postman.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.rest.RestService;

@Component
public class CommonServiceClient {

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Value("${bootjx.tunnel.scheduler}")
	private String scheduler;

	@Autowired
	RestService restService;

	@Autowired
	AppConfig appConfig;

}
