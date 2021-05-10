package com.boot.jx.postman.gupshup;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Configuration
@PropertySource("classpath:application-gupshup.properties")
@EnableEncryptableProperties
public class GupShupConfigClient {

	@Value("${gupshup.wa.number}")
	protected String gupShupWaNumber;

	@Value("${gupshup.api.url}")
	protected String gupShupApiUrl;

	@Value("${gupshup.agent.url}")
	protected String gupShupAgentUrl;

	@Value("${gupshup.notify.id}")
	protected String gupShupNotifyId;

	@Value("${gupshup.notify.pass}")
	protected String gupShupNotifyPass;

	@Value("${gupshup.chat.id}")
	protected String gupShupChatId;

	@Value("${gupshup.chat.pass}")
	protected String gupShupChatPass;

	public String getGupShupWaNumber() {
		return gupShupWaNumber;
	}

	public void setGupShupWaNumber(String gupShupWaNumber) {
		this.gupShupWaNumber = gupShupWaNumber;
	}

	public String getGupShupApiUrl() {
		return gupShupApiUrl;
	}

	public void setGupShupApiUrl(String gupShupApiUrl) {
		this.gupShupApiUrl = gupShupApiUrl;
	}

	public String getGupShupAgentUrl() {
		return gupShupAgentUrl;
	}

	public void setGupShupAgentUrl(String gupShupAgentUrl) {
		this.gupShupAgentUrl = gupShupAgentUrl;
	}

	public String getGupShupNotifyId() {
		return gupShupNotifyId;
	}

	public void setGupShupNotifyId(String gupShupNotifyId) {
		this.gupShupNotifyId = gupShupNotifyId;
	}

	public String getGupShupNotifyPass() {
		return gupShupNotifyPass;
	}

	public void setGupShupNotifyPass(String gupShupNotifyPass) {
		this.gupShupNotifyPass = gupShupNotifyPass;
	}

	public String getGupShupChatId() {
		return gupShupChatId;
	}

	public void setGupShupChatId(String gupShupChatId) {
		this.gupShupChatId = gupShupChatId;
	}

	public String getGupShupChatPass() {
		return gupShupChatPass;
	}

	public void setGupShupChatPass(String gupShupChatPass) {
		this.gupShupChatPass = gupShupChatPass;
	}

}
