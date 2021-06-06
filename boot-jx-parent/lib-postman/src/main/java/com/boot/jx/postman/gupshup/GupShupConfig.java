package com.boot.jx.postman.gupshup;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GupShupConfig implements Serializable {

	private static final long serialVersionUID = -2397678752642150000L;
	private String number;
	private String notifyId;
	private String notifyPass;
	private String chatId;
	private String chatPass;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getNotifyId() {
		return notifyId;
	}

	public void setNotifyId(String notifyId) {
		this.notifyId = notifyId;
	}

	public String getNotifyPass() {
		return notifyPass;
	}

	public void setNotifyPass(String notifyPass) {
		this.notifyPass = notifyPass;
	}

	public String getChatId() {
		return chatId;
	}

	public void setChatId(String chatId) {
		this.chatId = chatId;
	}

	public String getChatPass() {
		return chatPass;
	}

	public void setChatPass(String chatPass) {
		this.chatPass = chatPass;
	}

}
