package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;

@Component
@PropertySource("classpath:application-postman.properties")
public class ChatClientConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientConfig.class);

	public static class PATH {
		public static final String ASSIGN_TO_AGENT = "/int/assign/agent";
	}

	@Value("${postman.app.type}")
	private String postmanType;

	@Value("${postman.agent.url}")
	private String agentUrl;

	@Value("${postman.inbound.forward.url}")
	private String inboundForwardUrl;

	@Value("${postman.contact.details.url}")
	private String contactDetailsUrl;

	@Value("${postman.chat.dummy.user.enabled}")
	boolean chatDummyUserEnabled;

	@Value("${postman.chat.dummy.bot.enabled}")
	boolean chatDummyBotEnabled;

	@Value("${postman.chat.idle.timeout}")
	private String chatIdleTimeout;

	@Value("${postman.chat.session.timeout}")
	private String chatSessionTimeout;

	@Value("${postman.agent.session.timeout}")
	private String agentSessionTimeout;

	@Value("${postman.default.sender}")
	private String defaultSender;

	@Autowired
	private RestService restService;

	@Autowired
	private PMEnvironment environment;

	public boolean isChatDummyUserEnabled() {
		return chatDummyUserEnabled;
	}

	public boolean isChatDummyBotEnabled() {
		return chatDummyBotEnabled;
	}

	public String getAgentUrl() {
		return agentUrl;
	}

	public String getDefaultSender() {
		return ArgUtil.parseAsString(environment.config().agent().getDefaultBotName(), defaultSender);
	}

	public String getContactDetailsUrl() {
		return environment.config().get("postman.contact.details.url").asString(contactDetailsUrl);
	}

	public String getChatIdleTimeout() {
		return environment.config().get("postman.chat.idle.timeout").asString(chatIdleTimeout);
	}

	public String getInboundForwardUrl() {
		return inboundForwardUrl;
	}

	public void setInboundForwardUrl(String inboundForwardUrl) {
		this.inboundForwardUrl = inboundForwardUrl;
	}

	public String getPostmanType() {
		return postmanType;
	}

	public String getChatSessionTimeout() {
		return environment.config().get("postman.chat.session.timeout").asString(chatSessionTimeout);
	}

	public String getAgentSessionTimeout() {
		return agentSessionTimeout;
	}

}
