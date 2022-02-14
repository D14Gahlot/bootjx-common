package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO.ChatUserProfileRequest;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class ChatClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClient.class);

    public static class PATH {
	public static final String ASSIGN_TO_AGENT = "/int/assign/agent";
	public static final String INBOUND_FRWRD = "/int/inbound/callback";
    }

    @Autowired
    private RestService restService;

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private PMCommonConfig pmCommonConfig;

    public ApiResponse<InboxMessage, Object> forward(String inboundForwardUrl, InboxMessage inboxMessage) {
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

    public ApiResponse<InboxMessage, Object> forward(InboxMessage inboxMessage) {
	return this.forward(chatClientConfig.getInboundForwardUrl(), inboxMessage);
    }

    public ApiResponse<InboxMessage, Object> assignToAgent(InboxMessage inboxMessage) {
	LOGGER.debug("Assign InboxMessage Session to other Agent ");
	if (ArgUtil.is(pmCommonConfig.getAgentUrl())) {
	    inboxMessage.setChecksum(PostManUtil.generateCheckSum(inboxMessage));
	    return restService.ajax(pmCommonConfig.getAgentUrl()).path(PATH.ASSIGN_TO_AGENT).post(inboxMessage)
		    .as(new ParameterizedTypeReference<ApiResponse<InboxMessage, Object>>() {
		    });
	} else {
	    return null;
	}
    }

    public ChatUserProfileDTO fetchContactDetails(ChatUserProfileRequest chatUserProfileRequest) {
	if (ArgUtil.is(chatClientConfig.getContactDetailsUrl())) {
	    return restService.ajax(chatClientConfig.getContactDetailsUrl()).post(chatUserProfileRequest)
		    .as(new ParameterizedTypeReference<ChatUserProfileDTO>() {
		    });
	} else {
	    return null;
	}
    }

}
