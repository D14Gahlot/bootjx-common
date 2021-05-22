package com.boot.jx.common.config;

import java.io.Serializable;

public class AppCommonSessionBean implements Serializable {

	private static final long serialVersionUID = 3845458315325985248L;

	public String actorUsername;

	public String getActor() {
		return actorUsername;
	}

	public void setActor(String actor) {
		this.actorUsername = actor;
	}

}
