package com.boot.jx.postman.store;

import com.boot.utils.Constants;

public class PMStoreConstants extends Constants {
	public static final String SYSTEM = "__SYSTEM__";
	public static final String NO_DEPT = "__DEPT__";
	public static final String NO_USER = "__USER__";

	public static enum CHAT_STATUS {
		OPEN, UNASSIGNED, URGENT, ONHOLD, ATTENTION, EXPIRED, RESOLVED, CLOSED;
	}

	public static enum CHAT_MODE {
		AGENT, BOT;
	}

}