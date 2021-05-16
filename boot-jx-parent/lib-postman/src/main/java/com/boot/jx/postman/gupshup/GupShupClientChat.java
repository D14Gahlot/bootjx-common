package com.boot.jx.postman.gupshup;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.gupshup.GupShupConstants.DataEncoding;
import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;

@Component
public class GupShupClientChat extends GupShupClientAbstract implements MessageClient {

	@Override
	public SessionType getSessionType() {
		return SessionType.CHAT;
	}

	@Override
	public GupShupResp sendMessage(GupShupReq gupShupReq) {
		gupShupReq.method(GupShupConstants.Method.SendMessage).messageType(GupShupConstants.MessageType.DATA_TEXT);
		return post(gupShupReq);
	}

	@Override
	public GupShupResp sendDocumentURL(GupShupReq gupShupReq) {
		gupShupReq.method(GupShupConstants.Method.SendMediaMessage).messageType(GupShupConstants.MessageType.DOCUMENT);
		gupShupReq.setHsm(false);
		// gupShupReq.dataEncoding(DataEncoding.TEXT);
		gupShupReq.setFormat(GupShupConstants.Format.JSON);
		return post(gupShupReq);
	}

	@Override
	public GupShupResp sendImageURL(GupShupReq gupShupReq) {
		gupShupReq.method(GupShupConstants.Method.SendMediaMessage).messageType(GupShupConstants.MessageType.IMAGE);
		gupShupReq.setHsm(false);
		// gupShupReq.dataEncoding(DataEncoding.TEXT);
		gupShupReq.setFormat(GupShupConstants.Format.JSON);
		return post(gupShupReq);
	}

	@Override
	public GupShupResp sendAudioURL(GupShupReq gupShupReq) {
		gupShupReq.method(GupShupConstants.Method.SendMediaMessage).messageType(GupShupConstants.MessageType.AUDIO);
		gupShupReq.setHsm(false);
		gupShupReq.dataEncoding(DataEncoding.TEXT);
		gupShupReq.setFormat(GupShupConstants.Format.JSON);
		return post(gupShupReq);
	}

	@Override
	public GupShupResp sendVideoURL(GupShupReq gupShupReq) {
		gupShupReq.method(GupShupConstants.Method.SendMediaMessage).messageType(GupShupConstants.MessageType.VIDEO);
		gupShupReq.setHsm(false);
		gupShupReq.dataEncoding(DataEncoding.TEXT);
		gupShupReq.setFormat(GupShupConstants.Format.JSON);
		return post(gupShupReq);
	}
}
