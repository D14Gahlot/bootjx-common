package com.boot.jx.bot;

import java.io.Serializable;
import java.math.BigDecimal;

import com.boot.jx.postman.store.IChatContextStore.BasicChatContextUser;

public class ChatUser extends BasicChatContextUser implements Serializable {

	private static final long serialVersionUID = 7269291269502598338L;

	public static enum LoginStatus {
		GUEST, UNVERIFIED, VERIFIED,
	}

	private String identity;
	private BigDecimal customerId;

	private LoginStatus loginStatus;

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public LoginStatus getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(LoginStatus loginStatus) {
		this.loginStatus = loginStatus;
	}

	public BigDecimal getCustomerId() {
		return customerId;
	}

	public void setCustomerId(BigDecimal customerId) {
		this.customerId = customerId;
	}

}
