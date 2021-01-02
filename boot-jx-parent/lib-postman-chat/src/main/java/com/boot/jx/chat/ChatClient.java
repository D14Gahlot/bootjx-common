package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
@PropertySource("classpath:application-postman.properties")
public class ChatClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

	public static class PATH {
		public static final String ASSIGN_TO_AGENT = "/chat/assign/agent";
	}

	@Value("${postman.agent.url}")
	private String agentUrl;

	@Value("${postman.inbound.forward.url}")
	private String inboundForwardUrl;

	@Value("${postman.chat.dummy.user.enabled}")
	boolean chatDummyUserEnabled;

	@Value("${postman.chat.dummy.bot.enabled}")
	boolean chatDummyBotEnabled;

	@Autowired
	private RestService restService;

	public boolean isChatDummyUserEnabled() {
		return chatDummyUserEnabled;
	}

	public boolean isChatDummyBotEnabled() {
		return chatDummyBotEnabled;
	}

	public String getAgentUrl() {
		return agentUrl;
	}

	public ApiResponse<InboxMessage, Object> forward(InboxMessage inboxMessage) {
		LOGGER.debug("Forwarding InboxMessage to other Service ");
		try {
			if (ArgUtil.is(inboundForwardUrl)) {
				inboxMessage.setChecksum(PostManUtil.generateCheckSum(inboxMessage));
				return restService.ajax(inboundForwardUrl).post(inboxMessage)
						.as(new ParameterizedTypeReference<ApiResponse<InboxMessage, Object>>() {
						});
			}
		} catch (Exception e) {
			throw new PostManException(e);
		}
		return ApiResponse.buildResult(inboxMessage);
	}

	public ApiResponse<InboxMessage, Object> assignToAgent(InboxMessage inboxMessage) {
		LOGGER.debug("Assign InboxMessage Session to other Agent ");
		if (ArgUtil.is(this.agentUrl)) {
			inboxMessage.setChecksum(PostManUtil.generateCheckSum(inboxMessage));
			return restService.ajax(agentUrl).path(PATH.ASSIGN_TO_AGENT).post(inboxMessage)
					.as(new ParameterizedTypeReference<ApiResponse<InboxMessage, Object>>() {
					});
		} else {
			return null;
		}
	}

}
