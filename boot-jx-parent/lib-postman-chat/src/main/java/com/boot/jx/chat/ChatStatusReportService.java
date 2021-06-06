package com.boot.jx.chat;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.utils.ArgUtil;

@Component
public class ChatStatusReportService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatStatusReportService.class);

	ConcurrentLinkedQueue<MessageReport> queue = new ConcurrentLinkedQueue<MessageReport>();

	public static class MessageReportComparator implements Comparator<MessageReport> {
		@Override
		public int compare(MessageReport o1, MessageReport o2) {
			return (int) (o1.getTimestamp() - o2.getTimestamp());
		}
	}

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	public void offer(MessageReport e) {
		queue.offer(e);
	}

	public void offer(List<MessageReport> es) {
		for (MessageReport e : es) {
			queue.offer(e);
		}
	}

	@Async
	public void process(String batchId) {
		try {
			Thread.sleep(500L);
			List<MessageReport> batch = new LinkedList<MessageReport>();
			boolean hasElement = true;
			while (hasElement) {
				MessageReport e = queue.poll();
				hasElement = ArgUtil.is(e);
				if (hasElement) {
					batch.add(e);
				} else {
					break;
				}
			}
			LOGGER.debug("BATCH SIZE", batch.size());
			if (batch.size() > 0) {
				Collections.sort(batch, new MessageReportComparator());
				for (MessageReport messageReport : batch) {
					messageStore.updateStatus(messageReport);
					stompTunnelService.sendToAll("/message/update/status", messageReport);
				}
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}

	}

}
