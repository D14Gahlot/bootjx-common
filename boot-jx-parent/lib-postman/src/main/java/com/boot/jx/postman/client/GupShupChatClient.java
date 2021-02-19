package com.boot.jx.postman.client;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.gupshup.AbstractGupShupClient;
import com.boot.jx.postman.gupshup.GupShupAgentReq;
import com.boot.jx.postman.gupshup.GupShupConstants;
import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.gupshup.GupShupInboundV2;
import com.boot.jx.postman.gupshup.GupShupReq;
import com.boot.jx.postman.gupshup.GupShupResp;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.utils.ArgUtil;

@Component
public class GupShupChatClient extends AbstractGupShupClient {

	@Override
	public SessionType getSessionType() {
		return SessionType.CHAT;
	}

	@Override
	public GupShupResp sendMessage(String phoneNumber, String message) {
		return post(new GupShupReq(GupShupConstants.Method.SendMessage).sendTo(phoneNumber)
				.messageType(GupShupConstants.MessageType.TEXT).message(message));
	}

	public Map<String, Object> sendViaAgent(GupShupInbound innbound, String message) {
		GupShupAgentReq gupShupAgentReq = new GupShupAgentReq();
		gupShupAgentReq.setMobile(innbound.getMobile());
		gupShupAgentReq.setWaNumber(innbound.getWaNumber());
		gupShupAgentReq.setName(innbound.getName());
		gupShupAgentReq.setType(GupShupConstants.MessageType.text);
		gupShupAgentReq.setMsg(message);
		return this.restService.ajax(gupShupConfig.getGupShupAgentUrl()).path("/WhatsAppConnector/api")
				.header("type", "BotRequest").post(gupShupAgentReq).asMap();
	}

	public Map<String, Object> sendViaAgent(InboxMessage inboxMessage, String message) {
		GupShupAgentReq gupShupAgentReq = new GupShupAgentReq();
		gupShupAgentReq.setMobile(inboxMessage.getFrom());
		gupShupAgentReq.setWaNumber(inboxMessage.getTo());
		gupShupAgentReq.setName(inboxMessage.getFromName());
		gupShupAgentReq.setType(GupShupConstants.MessageType.text);
		gupShupAgentReq.setMsg(message);
		return this.restService.ajax(gupShupConfig.getGupShupAgentUrl()).path("/WhatsAppConnector/api")
				.header("type", "BotRequest").post(gupShupAgentReq).asMap();
	}

	public Map<String, Object> agentArchive(GupShupInbound innbound) {
		GupShupAgentReq gupShupAgentReq = new GupShupAgentReq();
		gupShupAgentReq.setMobile(innbound.getMobile());
		gupShupAgentReq.setWaNumber(innbound.getWaNumber());
		gupShupAgentReq.setMsg(innbound.getText());
		gupShupAgentReq.setType(innbound.getType());
		gupShupAgentReq.setName(innbound.getName());
		return this.restService.ajax(gupShupConfig.getGupShupAgentUrl()).path("/WhatsAppConnector/api")
				.header("type", "BotRequest").post(gupShupAgentReq).asMap();
	}

	public Map<String, Object> getToken(String waNumber, String mobile) {
		return this.restService.ajax(gupShupConfig.getGupShupAgentUrl()).path("/WhatsAppConnector/api")
				.header("type", "getToken").field("userId", gupShupConfig.getGupShupChatId())
				.field("password", gupShupConfig.getGupShupChatPass()).field("phoneNo", mobile)
				.field("waNumber", waNumber).postForm().asMap();
	}

	public Map<String, Object> assignToAgent(String waNumber, String mobile, String deptName) {
		String token = ArgUtil.parseAsString(this.getToken(waNumber, mobile).get("token"));
		Ajax x = this.restService.ajax(gupShupConfig.getGupShupAgentUrl()).path("/WhatsAppConnector/api")
				.header("type", "TransferRequestToAgent").field("token", token).field("phoneNo", mobile)
				.field("waNumber", waNumber);
		if (ArgUtil.is(deptName)) {
			x.field("deptName", deptName);
		}
		return x.postForm().asMap();
	}

	public GupShupInbound parseAsGupShupInbound(GupShupInboundV2 inboundV2) {
		GupShupInbound inb = new GupShupInbound();
		inb.setWaNumber(inboundV2.getContacts().get(0).getWaId());
		inb.setMobile(inboundV2.getMessages().get(0).getFrom());
		inb.setName(inboundV2.getContacts().get(0).getProfile().getName());
		inb.setText(inboundV2.getMessages().get(0).getText().getBody());
		inb.setType(inboundV2.getMessages().get(0).getType());
		inb.setTimestamp(inboundV2.getMessages().get(0).getTimestamp());
		inb.setReplyId(inboundV2.getMessages().get(0).getId());
		return inb;
	}

	public InboxMessage parseAsInboxMessage(GupShupInbound inbound) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ContactType.WHATSAPP);
		inboxMessage.setChannel(Channel.GUPSHUP.toString());
		inboxMessage.from(inbound.getMobile());
		inboxMessage.setFromName(inbound.getName());
		inboxMessage.setMessage(inbound.getText());
		inboxMessage.setTo(inbound.getWaNumber());
		inboxMessage.setMessageIdExt(inbound.getReplyId());
		return inboxMessage;
	}

	public InboxMessage parseAsInboxMessage(GupShupInboundV2 inboundV2) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ContactType.WHATSAPP);
		inboxMessage.setChannel(Channel.GUPSHUP.toString());
		inboxMessage.from(inboundV2.getMessages().get(0).getFrom());
		inboxMessage.setFromName(inboundV2.getContacts().get(0).getProfile().getName());
		inboxMessage.setMessage(inboundV2.getMessages().get(0).getText().getBody());
		inboxMessage.setTo(inboundV2.getContacts().get(0).getWaId());
		inboxMessage.setMessageIdExt(inboundV2.getMessages().get(0).getId());
		return inboxMessage;
	}

}
