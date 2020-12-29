package com.boot.jx.postman.client;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.gupshup.AbstractGupShupClient;
import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;

@Component
public class GupShupNotifyClient extends AbstractGupShupClient {

	@Override
	public SessionType getSessionType() {
		return SessionType.NOTIFICATION;
	}

	@Override
	public boolean getIsHSM() {
		return true;
	}

}
