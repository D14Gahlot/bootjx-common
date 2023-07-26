package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.cache.OtpAlertEvent;
import com.boot.jx.contak.cache.OtpAlertEventManager;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.ContakInboundDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;

@Component
public class ContakInboundRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContakInboundRouter.class);

	public static class USER_INBOUND_TYPE {
		public static final String USER_REGISTERED = "USER_REGISTERED";
		public static final String USER_LOGIN = "USER_LOGIN"; // VALIDATE
		public static final String USER_RELOGIN = "USER_RELOGIN"; // VERIFY
		public static final String MSG_OUT_DELIVERED = "MSG_OUT_DELIVERED"; // MSG_OUT_READ
		public static final String MSG_OUT_READ = "MSG_OUT_READ"; // MSG_OUT_READ
		public static final String MSG_OUT_LOG = "MSG_OUT_LOG"; // MSG_OUT_READ
	}

	@Autowired
	private ContakInboundManager contakInboundManager;

	@Autowired
	private OtpAlertEventManager otpAlertEventManager;

	public List<ContakInboundDoc> fetchInbounds(String companyId) {
		return contakInboundManager.fetchInbounds(companyId);
	}

	public void sendHandShakeAckEvent(UserRegistrationDoc userRegistrationDoc) {
		contakInboundManager.sendHandShakeAckEvent(userRegistrationDoc);
	}

	public void sendMsgDelvryEvent(ContakMessageDoc contakMessageDoc) {
		contakInboundManager.sendMsgDelvryEvent(contakMessageDoc);
	}

	public void sendMsgDelvryEventAsync(List<ContakMessageDoc> messages) {
		contakInboundManager.sendMsgDelvryEventAsync(messages);
	}

	public void sendMsgReadEvent(ContakMessageDoc contakMessageDoc) {
		contakInboundManager.sendMsgReadEvent(contakMessageDoc);
	}

	public void sendMsgReadEventAsync(List<ContakMessageDoc> messages) {
		contakInboundManager.sendMsgReadEventAsync(messages);
	}

	public void sendUserAuthEvent(PhoneUserDoc phoneUserDoc, String isUserRegistraion) {
		contakInboundManager.sendUserAuthEvent(phoneUserDoc, isUserRegistraion);
	}

	@Async
	public void sendMsgLogEventAsync(ContakMessageDoc contakMessageDoc, MessageEvent event) {
		contakInboundManager.sendMsgLogEventAsync(contakMessageDoc, event);
	}

	public List<OtpAlertEvent> pollOtpAlertEvents(String companyId) {
		return otpAlertEventManager.pollOtpAlertEvents(companyId);
	}

	public OtpAlertEvent createOtpAlertEvent(String generateString, String companyId, String phoneId) {
		return otpAlertEventManager.createOtpAlertEvent(generateString, companyId, phoneId);
	}

}
