package com.boot.jx.postman.others;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.OAPlugin.OAConfigDetails;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.JsonPath;

@Component
public class OAClient {

	public static final String TEXTLOCAL = "TEXTLOCAL";
	public static final String TEXTLOCAL_URL = "https://api.otp.in/send";
	public static final JsonPath TEXTLOCAL_URL_RESPONSE_MSG_ID = new JsonPath("messages/[0]/id");

	public static final String TWILIO = "TWILIO";
	public static final String TWILIO_URL = "https://api.twilio.com/2010-04-01/";
	public static final JsonPath TWILIO_URL_RESPONSE_MSG_ID = new JsonPath("sid");

	@Autowired
	private RestService restService;

	public OutboxMessage sendMessage(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
		OAConfigDetails oa = channelConfig.getOa();
		MapModel resp = restService.ajax(oa.getClientId() + ".otpalerts.com/entoc/api/v1/e2ee/send")
				.header("x-api-key", oa.getApiKey()).postJson(MapModel.createInstance()
						//
						.put("phone", outboxMessage.contact().getPhone())
						.put("template.code", outboxMessage.getTemplateExt())
						.put("template.model", outboxMessage.getModel())
						//
						.toMap())
				.asMapModel();;
		return outboxMessage;
	}

}
