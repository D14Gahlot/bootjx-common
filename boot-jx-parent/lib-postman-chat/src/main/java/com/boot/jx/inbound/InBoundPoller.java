package com.boot.jx.inbound;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.utils.ArgUtil;

@EnableScheduling
@Component
public class InBoundPoller extends ATaskLimiter {

	public static final String TASK_EMAIL_POLLER = "EMAIL_POLLER";

	private static final Logger LOGGER = LoggerService.getLogger(InBoundPoller.class);

	@Autowired
	private AppConfig appConfig;

	@Override
	public void doTask(TunnelTask task) {
		if (ArgUtil.is(task.getName(), TASK_EMAIL_POLLER)) {

		}
	}

}
