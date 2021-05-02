package com.boot.jx.postman.gupshup;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;

@Component
public class GupShupClientNotify extends GupShupClientAbstract {

	@Override
	public SessionType getSessionType() {
		return SessionType.NOTIFICATION;
	}

	@Override
	public boolean getIsHSM() {
		return true;
	}

}
