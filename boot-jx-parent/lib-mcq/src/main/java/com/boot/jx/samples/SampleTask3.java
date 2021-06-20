package com.boot.jx.samples;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.jx.AppConfig;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mcq.shedlock.SchedulerLock;

//@Configuration
//@EnableScheduling
//@Component
//@Service
public class SampleTask3 {

	private static final Logger LOGGER = LoggerService.getLogger(SampleTask3.class);

	@Autowired
	AppConfig appConfig;

	@SchedulerLock(lockMaxAge = 60000, name = "someTask")
	@Scheduled(fixedDelay = 5000)
	public void doTask() throws InterruptedException {
		Thread.sleep(3000);
		LOGGER.info("======= I am doing my Task @ {}", appConfig.getSpringAppName());
	}

}
