package com.boot.jx.postman.others;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.CryptoUtil;

@Component
public class TextLocalClient {

	public static final String BASE_URL = "https://api.textlocal.in";

	@Autowired
	private RestService restService;

	public OutboxMessage sendSMS(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
		MapModel resp = restService.ajax(BASE_URL).path("/send").pathParam("apikey", channelConfig.getTwilio().getSid())
				.field("sender", channelConfig.getTwilio().getNumber())
				.field("numbers", outboxMessage.contact().getCsid()).field("message", outboxMessage.getMessage())
				.postForm().asMapModel();
		System.out.println(resp.toJson());
		return outboxMessage;
	}
}
