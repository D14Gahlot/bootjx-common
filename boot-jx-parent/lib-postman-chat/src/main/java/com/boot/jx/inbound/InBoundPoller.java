package com.boot.jx.inbound;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.logger.LoggerService;

@EnableScheduling
@Component
public class InBoundPoller {

	private static final Logger LOGGER = LoggerService.getLogger(InBoundPoller.class);

	@Autowired
	AppConfig appConfig;

	@Scheduled(fixedDelay = 5000)
	public void doTask() throws InterruptedException {
		//LOGGER.info("======= I am doing my Task @ {}", appConfig.getSpringAppName());
	}

}
