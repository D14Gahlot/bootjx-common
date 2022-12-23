package com.boot.jx.postman.others;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.SMSPlugin.SMSConfigDetails;
import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.model.MapModel;
import com.boot.utils.JsonPath;

@Component
public class SMSClient {

	public static final String TEXTLOCAL = "TEXTLOCAL";
	public static final String TEXTLOCAL_URL = "https://api.textlocal.in/send";
	public static final JsonPath TEXTLOCAL_URL_RESPONSE_MSG_ID = new JsonPath("messages/[0]/id");

	@Autowired
	private RestService restService;

	public OutboxMessage sendSMS(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
		SMSConfigDetails sms = channelConfig.getSms();

		MapModel pub = MapModel.from(sms.getPub());
		MapModel secret = MapModel.from(sms.getSecret());

		if (TEXTLOCAL.equalsIgnoreCase(sms.getProvider())) {
			MapModel resp = restService.ajax(TEXTLOCAL_URL).field("apikey", secret.entry("apikey").asString())
					.field("sender", channelConfig.getSms().getNumber())
					.field("numbers", outboxMessage.contact().getCsid()).field("message", outboxMessage.getMessage())
					.submit().asMapModel();
			outboxMessage.setMessageIdExt(resp.entry(TEXTLOCAL_URL_RESPONSE_MSG_ID).asString());
		} else {
			Ajax ajax = restService.ajax(sms.getRequest().getUrl());
			if (sms.getRequest().getHeaders() != null) {
				for (String header : sms.getRequest().getHeaders()) {
					String[] hd = header.split(":");
					ajax.header(hd[0], hd[1]);
				}
			}

			if ("POST".equalsIgnoreCase(sms.getRequest().getMethod())) {
				if (sms.getRequest().getFields() != null) {
					for (Entry<String, String> field : sms.getRequest().getFields().entrySet()) {
						ajax.field(field.getKey(), field.getValue());
					}
					ajax.submit().asNone();
				} else if (sms.getRequest().getData() != null) {
					ajax.postJson(sms.getRequest().getData()).asNone();
				}
			} else if ("GET".equalsIgnoreCase(sms.getRequest().getMethod())) {
				if (sms.getRequest().getFields() != null) {
					for (Entry<String, String> field : sms.getRequest().getFields().entrySet()) {
						ajax.queryParam(field.getKey(), field.getValue());
					}
				}
				ajax.get().asNone();
			}
		}

		return outboxMessage;
	}
}
