package com.boot.jx.cache.test;

import java.util.Date;

import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;

//@Component
public class RedisSampleTaskLimiter extends ATaskLimiter {

	@Override
	public void doTask(TunnelTask task) {
		System.out.println(String.format("%s - %s %s", new Date().toString(), task.getId(), task.getName()));
	}

}
