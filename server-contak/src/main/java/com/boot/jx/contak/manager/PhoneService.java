package com.boot.jx.contak.manager;

import java.util.Map.Entry;

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
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
public class PhoneService {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private PMEnvironment pmEnvironment;

	private OutboxMessage send(ChannelConfig channel, OutboxMessage outboxMessage) {
		if (!ArgUtil.is(channel)) {
			return outboxMessage;
		}
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

	public OutboxMessage send(String channelId, OutboxMessage outboxMessage) {
		ChannelConfig channel = pmEnvironment.config().channel(channelId);
		return send(channel, outboxMessage);
	}

	public OutboxMessage sendPhoneOTP(String phone, String otp) {
		OutboxMessage ob = new OutboxMessage();
		ob.contact().phone(phone);
		ob.setHsm(new CommonTemplateMeta().code("verification_otp")
				.data(MapModel.createInstance().put("otp", otp).toMap()));

		String countryCode = "IN";
		try {
			phone = phone.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();
			PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse("+" + phone, countryCode);
			phone = String.format("+%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			countryCode = PHONE_NUMBER_UTIL.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
		} catch (NumberParseException e) {
			phone = String.format("+%s", phone);
		}

		String defchannelId = null;
		for (Entry<String, ChannelConfig> channel : pmEnvironment.local().channels().entrySet()) {
			if (ArgUtil.is(channel.getValue().getSms())
					&& ArgUtil.is(channel.getValue().getSms().getCountry(), countryCode)) {
				return send(channel.getValue().getChannelId(), ob);
			} else if (ArgUtil.is(channel.getValue().getSms())
					&& ArgUtil.not(channel.getValue().getSms().getCountry())) {
				defchannelId = channel.getValue().getChannelId();
			}
		}
		return send(defchannelId, ob);
	}

	public OutboxMessage sendEmailOTP(String email, String otp) {
		OutboxMessage ob = new OutboxMessage();
		ob.contact().setEmail(email);
		ob.setHsm(new CommonTemplateMeta().code("verification_otp")
				.data(MapModel.createInstance().put("otp", otp).toMap()));
		return send("mailto:meheryxyzgmailcom", ob);
	}

}
