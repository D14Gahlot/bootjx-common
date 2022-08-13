package com.boot.jx.inbound;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.bot.BotEngine;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.chat.ChatProxyManager;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.inbound.InBound.InBoundFilter;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.inbound.InBound.InBoundProcessor;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ErrorObject;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.StringUtils.StringMatcher;
import com.boot.utils.UniqueID;

@Component
public class InBoundService extends ATaskLimiter {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundService.class);
	public static final Pattern PROXY = Pattern.compile("\\/proxy\\ ([a-zA-Z0-9_\\-]+)$");
	public static final Pattern UNPROXY = Pattern.compile("\\/unproxy\\ ([a-zA-Z0-9_\\-]+)$");

	@Autowired(required = false)
	private InBoundProcessor inBoundProcessor;

	@Autowired(required = false)
	private InBoundFilter inBoundFilter;

	@Autowired(required = false)
	private InBoundHandler inBoundHandler;

	@Autowired
	private BotEngine botEngine;

	@Autowired
	private AppConfig aapConfig;

	@Autowired
	private PMClientConfig chatClientConfig;

	@Autowired
	private ChatStatusService chatStatusService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private MessageContext messageContext;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired(required = false)
	private RedissonClient redisson;

	@Autowired
	private ChatProxyManager proxyManager;

	@Autowired
	private ChatLogger logManager;

	/**
	 * Invoke the methods with matching {@link ChatMapping#events()} and
	 * {@link ChatMapping#pattern()} in events received from Slack/Facebook.
	 *
	 * @param event received from facebook
	 */
	@Async
	public void pushMessageToInvokeAsync(InboxMessage inboxMessageOriginal) {
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());
		boolean onhold = proxyManager.onhold(contactId);
		// System.out.println("===>" + onhold);

		messageStore.original(inboxMessageOriginal);

		if (onhold) {
			messageStore.hold(inboxMessageOriginal);
			throttle(new TunnelTask().name("MESSAGE_RELEASE").id(contactId).intervalSeconds(10));
		} else {
			proxyManager.hold(contactId);
			// hold().put(contactId, "HOLDING");
			// messageStore.hold(inboxMessageOriginal);
			invokeMethodsInternalSafely(inboxMessageOriginal, true);
			// if (inboxMessageOriginal.session().isFirstMessage()) {
			// this.invokeMethodsRelease(inboxMessageOriginal);
			// }
			proxyManager.release(contactId);
			// hold().put(contactId, "RELEASING");
		}
		onhold = proxyManager.onhold(contactId);
		if (!onhold) {
			// System.out.println("===<" + onhold);
			this.invokeMethodsRelease(inboxMessageOriginal);
		}

	}

	public void invokeMethodsRelease(InboxMessage inboxMessageOriginal) {
		logManager.trace(inboxMessageOriginal, "InBoundService:invokeMethodsRelease");
		List<InboxMessage> msgs = messageStore.releaseBySession(inboxMessageOriginal);
		for (InboxMessage inboxMessage : msgs) {
			this.invokeMethodsInternalSafely(inboxMessage, false);
		}
	}

	public InboxMessage invokeMethods(InboxMessage inboxMessageOriginal) {
		return this.invokeMethodsInternalSafely(inboxMessageOriginal, false);
	}

	private InboxMessage invokeMethodsInternalSafely(InboxMessage inboxMessageOriginal, boolean newThread) {
		try {
			logManager.trace(inboxMessageOriginal, "InBoundService:invokeMethodsInternalSafely");
			return this.invokeMethodsInternal(inboxMessageOriginal, newThread);
		} catch (Exception e) {
			messageStore.reject(inboxMessageOriginal);
		}
		return inboxMessageOriginal;
	}

	private InboxMessage invokeMethodsInternal(InboxMessage inboxMessageOriginal, boolean newThread) {

		PMConfigurationObject proxyConfig = pmEnvironment.keyEntry("mry.proxy.enabled");
		String contactId = PostManUtil.CONTACT_ID(inboxMessageOriginal.contact());

		if ((AppContextUtil.getTenant().equals("app") || proxyConfig.asBoolean())) {
			String proxy = null;
			String message = ArgUtil.nonEmpty(inboxMessageOriginal.getMessage(), Constants.BLANK);

			StringMatcher matcher = new StringMatcher(message);
			if (matcher.isMatch(PROXY)) {
				proxy = matcher.group(1);
				proxyManager.put(contactId, proxy);
				return inboxMessageOriginal;
			} else if (matcher.isMatch(UNPROXY)) {
				proxyManager.fastRemove(contactId);
				return inboxMessageOriginal;
			} else {
				proxy = proxyManager.get(contactId);
			}

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

		ChatSessionDoc session = null;
		boolean locallySessionAssigned = false;
		if (ArgUtil.isEmpty(inboxMessageOriginal.getSessionId())
				|| "POSTMAN".equalsIgnoreCase(aapConfig.getAppType())) {
			session = chatSessionFactory.getChatSession(inboxMessageOriginal);
			if (ArgUtil.is(session)) {
				chatSessionFactory.linkSession(session, inboxMessageOriginal);
				locallySessionAssigned = true;
				messageContext.session(session);
			} else {
				ErrorObject error = new ErrorObject();
				error.setIncomingMessage(inboxMessageOriginal);
				error.setErrorType("NO_SESSION_CREATED");
				error.setMessage("Cannot Create Session");
				messageContext.log(error);
				return inboxMessageOriginal;
			}
		}

		if (ArgUtil.isEmpty(inboxMessageOriginal.getMessageId())) {
			inboxMessageOriginal.setMessage(StringUtils.trim(inboxMessageOriginal.getMessage()));
			MessageDoc messageDoc = messageStore.createOrUpdate(inboxMessageOriginal);
			chatSessionFactory.push(messageDoc, inboxMessageOriginal);
		}

		messageContext.setInboxMessage(inboxMessageOriginal);

		if (locallySessionAssigned && ArgUtil.is(session)) {
			boolean wasSessionInitd = session.isInitd();
			boolean isSessionInitd = chatSessionService.initSession(inboxMessageOriginal, session);
			if (!isSessionInitd) {
				return inboxMessageOriginal;
			}
			if (isSessionInitd && (wasSessionInitd != isSessionInitd)) {
				chatSessionService.initSessionPost(inboxMessageOriginal, session);
			}

		}

		logManager.trace(inboxMessageOriginal, "InBoundService:invokeMethodsInternal");

		if (ArgUtil.isEmpty(inBoundFilter) || inBoundFilter.doFilter(inboxMessageOriginal)) {
			if (ArgUtil.is(inBoundProcessor)) {
				inBoundProcessor.process(inboxMessageOriginal);
			}
			if (chatClientConfig.isLocalDummyBotEnabled()) {
				botEngine.invokeMethodsAsync(inboxMessageOriginal);
			} else if (ArgUtil.is(inBoundHandler)) {
				if (newThread) {
					inBoundHandler.onMessage(inboxMessageOriginal, session);
				} else {
					inBoundHandler.onMessageAsync(inboxMessageOriginal, session);
				}
			} else if (botEngine.isChatBotDefined()) { // TODO:-- TO be removed
				botEngine.invokeMethodsAsync(inboxMessageOriginal);
			}
		}
		return inboxMessageOriginal;
	}

	public void updateBatch(List<MessageReport> messageReports) {
		chatStatusService.offer(messageReports);
		chatStatusService.process(null);
	}

	@Async
	public void updateAsync(List<MessageReport> messageReports) {
		chatStatusService.update(messageReports);
	}

	@Override
	public void doTask(TunnelTask task) {
		if ("MESSAGE_RELEASE".equals(task.getName())) {
			String contactId = task.getId();
			proxyManager.release(contactId);
			InboxMessage msg = new InboxMessage();
			msg.contact().setContactId(contactId);
			msg.setContact(msg.contact());
			invokeMethodsRelease(msg);
		}
	}

}
