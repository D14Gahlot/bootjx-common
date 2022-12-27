package com.boot.jx.contak.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
public class PhoneService {

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private PMEnvironment pmEnvironment;

	public OutboxMessage send(String channelId, OutboxMessage outboxMessage) {
		ChannelConfig channel = pmEnvironment.config().channel(channelId);

		outboxMessage.contact().type(channel.getContactType());
		outboxMessage.contact().setChannelType(channel.getChannelType());
		outboxMessage.contact().setLane(channel.getLane());

		ChatSessionDoc chatSessionDoc = chatSessionFactory.linkSession(outboxMessage);

		if (ArgUtil.is(chatSessionDoc)) {
			chatSessionService.initSession(outboxMessage, chatSessionDoc);
			chatService.send(chatSessionDoc, outboxMessage);
		} else {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("to").obzect("OutBoundMsg").codeKey("INSUFFICIENT_CONTACT_DETAILS")
							.description("Session Cannot be initialized for given contact"));
		}
		String messageId = outboxMessage.getMessageId();
		return outboxMessage;
	}

	public OutboxMessage sendPhoneOTP(String phone, String otp) {
		OutboxMessage ob = new OutboxMessage();
		ob.contact().setPhone(phone);
		ob.setHsm(new CommonTemplateMeta().code("verification_otp")
				.data(MapModel.createInstance().put("otp", otp).toMap()));
		return send("sms:mehotp", ob);
	}

	public OutboxMessage sendEmailOTP(String email, String otp) {
		OutboxMessage ob = new OutboxMessage();
		ob.contact().setEmail(email);
		ob.setHsm(new CommonTemplateMeta().code("verification_otp")
				.data(MapModel.createInstance().put("otp", otp).toMap()));
		return send("mailto:meheryxyzgmailcom", ob);
	}
}
