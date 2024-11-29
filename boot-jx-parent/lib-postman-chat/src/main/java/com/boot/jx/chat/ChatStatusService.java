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

import com.boot.jx.AppContextUtil;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.stomp.StompTunnelService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;

@Component
public class ChatStatusService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatStatusService.class);

	ConcurrentLinkedQueue<MessageReport> queue = new ConcurrentLinkedQueue<MessageReport>();

	public static class MessageReportComparator implements Comparator<MessageReport> {
		@Override
		public int compare(MessageReport o1, MessageReport o2) {
			return (int) (o1.getChangeStamp() - o2.getChangeStamp());
		}
	}

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private StompTunnelService stompTunnelService;

	@Autowired(required = false)
	private InBoundHandler inBoundHandler;

	@Autowired
	private ChatProxyManager proxyManager;

	@Autowired(required = false)
	private MessageContext messageContext;

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
				update(batch);
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	public void update(List<MessageReport> batch) {
		for (MessageReport messageReport : batch) {
			// Check for Proxy Account
			String contactId = PostManUtil.createContactId(messageReport.contact());
			if (ArgUtil.is(contactId)) {
				String proxy = proxyManager.get(contactId);
				if (ArgUtil.is(proxy)) {
					AppContextUtil.clear();
					AppContextUtil.setTenant(proxy);
					String sessionId = UniqueID.generateString();
					AppContextUtil.setSessionId(sessionId);
					AppContextUtil.getTraceId(true, true);
					AppContextUtil.resetTraceTime();
					AppContextUtil.init();
				}
			}
			MessageDoc m = messageStore.updateStatus(messageReport);
			if (ArgUtil.is(inBoundHandler)) {
				if (ArgUtil.is(messageContext) && ArgUtil.is(m)) {
					messageContext.setMessageDoc(m);
				}
				inBoundHandler.doHandle(messageReport);
			} else {
				stompTunnelService.sendToAll("/message/update/status", messageReport);
			}

		}
	}

}
