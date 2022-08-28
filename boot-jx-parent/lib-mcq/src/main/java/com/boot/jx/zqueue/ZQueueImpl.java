package com.boot.jx.zqueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.ZQueueDefs.ZQueue;
import com.boot.jx.tunnel.ZQueueDefs.ZQueueElement;
import com.boot.jx.tunnel.ZQueueDefs.ZQueueStore;
import com.boot.utils.ArgUtil;

@Component
public class ZQueueImpl implements ZQueue {

	@Autowired(required = false)
	private ZQueueStore zQStore;

	@Lazy
	@Autowired(required = false)
	private ZQueueEngine zQueueEngine;

	@Override
	public void push(ZQueueElement element) {
		if (ArgUtil.is(zQStore)) {
			zQStore.enqueue(element);
			if (ArgUtil.is(zQueueEngine)) {
				zQueueEngine.throttleQ(new TunnelTask().name(element.getQueueType()).id(element.getQueueId()));
			}
		}
	}

	@Async
	public void pushBackAsync(TunnelTask task) {
		this.zQueueEngine.throttleQ(
				new TunnelTask().name(task.getName()).id(task.getId()).intervalMillis(task.getInterval() + 1000));
	}

}
