package com.boot.jx.postman.gupshup;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.utils.ArgUtil;

@Component
public class GupShupClientAgent extends GupShupClientAbstract {

	@Override
	public SessionType getSessionType() {
		return SessionType.AGENT;
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

	public Map<String, Object> sendViaAgent(IMessageExtended inboxMessage, String message) {
		GupShupAgentReq gupShupAgentReq = new GupShupAgentReq();
		gupShupAgentReq.setMobile(inboxMessage.getFrom());
		gupShupAgentReq.setWaNumber(inboxMessage.to().get(0));
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

}
