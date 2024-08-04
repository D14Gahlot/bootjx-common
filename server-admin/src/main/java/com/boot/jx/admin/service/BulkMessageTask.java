package com.boot.jx.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.tunnel.ChronoTask.ChronoTaskEvent;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;

@Component
@TunnelEventMapping(topic = "BulkMessageTask", scheme = TunnelEventXchange.TASK_WORKER)
public class BulkMessageTask implements ITunnelSubscriber<ChronoTaskEvent> {

	@Autowired
	private BulkMessageService bulkMessageService;

	@Override
	public void onListen(String channel, ChronoTaskEvent message) {
		bulkMessageService.registerJob(message.data().as(BatchJob.class));
	}

}
