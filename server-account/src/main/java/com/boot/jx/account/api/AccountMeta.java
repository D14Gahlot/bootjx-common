package com.boot.jx.account.api;

import java.io.Serializable;

public class AccountMeta implements Serializable {

    private static final long serialVersionUID = 6990839577469827351L;

    private boolean phoneVerified;
    private boolean emailVerified;
    private String emailVerificationCode;

    public String getEmailVerificationCode() {
	return emailVerificationCode;
    }

    public void setEmailVerificationCode(String emailVerificationCode) {
	this.emailVerificationCode = emailVerificationCode;
    }

    public boolean isPhoneVerified() {
	return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
	this.phoneVerified = phoneVerified;
    }

    public boolean isEmailVerified() {
	return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
	this.emailVerified = emailVerified;
    }
}
