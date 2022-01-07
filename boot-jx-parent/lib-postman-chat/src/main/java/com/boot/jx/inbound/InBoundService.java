package com.boot.jx.inbound;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.agent.AgentService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.bot.BotEngine;
import com.boot.jx.bot.ChatMapping;
import com.boot.jx.cache.CacheBox;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatService;
import com.boot.jx.def.ICacheBox;
import com.boot.jx.inbound.InBound.InBoundFilter;
import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.inbound.InBound.InBoundProcessor;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ErrorObject;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils.StringMatcher;
import com.boot.utils.UniqueID;

@Component
public class InBoundService {

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
    private ChatClient chatClient;

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private MessageContext messageContext;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired(required = false)
    private RedissonClient redisson;
    private CacheBox<String> proxyManager;

    public ICacheBox<String> proxy() {
	if (proxyManager == null) {
	    this.proxyManager = CacheBox.getInstance("InBoundService-Proxy", redisson);
	}
	return this.proxyManager;
    }

    /**
     * Invoke the methods with matching {@link ChatMapping#events()} and
     * {@link ChatMapping#pattern()} in events received from Slack/Facebook.
     *
     * @param event received from facebook
     */
    @Async
    public void invokeMethodsAsync(InboxMessage inboxMessageOriginal) {
	invokeMethods(inboxMessageOriginal);
    }

    public InboxMessage invokeMethods(InboxMessage inboxMessageOriginal) {

	PMConfigurationObject proxyConfig = pmEnvironment.keyEntry("mry.proxy.enabled");

	if ((AppContextUtil.getTenant().equals("app") || proxyConfig.asBoolean())
		&& ArgUtil.is(inboxMessageOriginal.getMessage()) && ArgUtil.is(redisson)) {
	    String contactId = PostManUtil.createContactId(inboxMessageOriginal.contact());
	    String proxy = null;

	    StringMatcher matcher = new StringMatcher(inboxMessageOriginal.getMessage());
	    if (matcher.isMatch(PROXY)) {
		proxy = matcher.group(1);
		proxy().put(contactId, proxy);
		return inboxMessageOriginal;
	    } else if (matcher.isMatch(UNPROXY)) {
		proxy().fastRemove(contactId);
		return inboxMessageOriginal;
	    } else {
		proxy = proxy().get(contactId);
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
		|| "POSTMAN".equalsIgnoreCase(chatClientConfig.getPostmanType())) {
	    session = sessionStore.createSession(inboxMessageOriginal);
	    if (ArgUtil.is(session)) {
		sessionStore.linkSession(session, inboxMessageOriginal);
		locallySessionAssigned = true;
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
	    sessionStore.push(messageDoc, inboxMessageOriginal);
	}

	messageContext.setMessage(inboxMessageOriginal);

	if (locallySessionAssigned && ArgUtil.is(session)) {
	    boolean wasSessionInitd = session.isInitd();
	    boolean isSessionInitd = chatService.initSession(inboxMessageOriginal, session);
	    if (!isSessionInitd) {
		return inboxMessageOriginal;
	    }
	    if (isSessionInitd && (wasSessionInitd != isSessionInitd)) {
		chatService.initSessionPost(inboxMessageOriginal, session);
	    }

	}

	if (ArgUtil.isEmpty(inBoundFilter) || inBoundFilter.onFilter(inboxMessageOriginal)) {
	    if (ArgUtil.is(inBoundProcessor)) {
		inBoundProcessor.process(inboxMessageOriginal);
	    }
	    if (chatClientConfig.isLocalDummyBotEnabled()) {
		botEngine.invokeMethodsAsync(inboxMessageOriginal);
	    } else if (ArgUtil.is(inBoundHandler)) {
		inBoundHandler.handle(inboxMessageOriginal);
	    } else if (agentService.onMessageSupported(inboxMessageOriginal)) { // TODO:-- TO be removed
		agentService.onMessage(inboxMessageOriginal);
	    } else if (botEngine.isChatBotDefined()) { // TODO:-- TO be removed
		botEngine.invokeMethodsAsync(inboxMessageOriginal);
	    } else { // TODO:-- TO be removed
		chatClient.forward(inboxMessageOriginal);
	    }
	}
	return inboxMessageOriginal;
    }

    public ApiResponse<InboxMessage, ?> assignToAgent(InboxMessage inboxMessageOriginal) {
	return agentService.assignToAgent(inboxMessageOriginal);
    }

}
