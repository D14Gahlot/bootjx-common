package com.boot.jx.common.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;

@Component
@ConditionalOnProperty(name = "mry.jobs.bulk.message.task", havingValue = "true", matchIfMissing = false)
@TunnelEventMapping(topic = "BulkMessageTask", scheme = TunnelEventXchange.TASK_WORKER)
public class BulkMessageTask implements ITunnelSubscriber<BatchJob> {

	@Autowired
	private BulkMessageService bulkMessageService;

	@Override
	public void onListen(String channel, BatchJob message) {
		bulkMessageService.registerJobAndTriggerSummary(message);
	}

}
