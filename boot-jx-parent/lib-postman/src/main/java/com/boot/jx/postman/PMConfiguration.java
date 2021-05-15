package com.boot.jx.postman;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.agent.AgentConfig;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;
import com.boot.utils.ArgUtil;

public class PMConfiguration implements Serializable {

	private static final long serialVersionUID = -5432956433673368768L;

	Map<String, FacebookConfig> facebook;
	Map<String, TwitterConfig> twitter;
	Map<String, TelegramConfig> telegram;
	Map<String, GupShupConfig> gupshup;

	AgentConfig agent;

	// Facebook
	public Map<String, FacebookConfig> facebook() {
		if (ArgUtil.isEmpty(facebook)) {
			facebook = new HashMap<String, FacebookConfig>();
		}
		return facebook;
	}

	public Map<String, FacebookConfig> getFacebook() {
		return facebook;
	}

	public void setFacebook(Map<String, FacebookConfig> facebook) {
		this.facebook = facebook;
	}

	public FacebookConfig facebook(String pageId) {
		return facebook.get(pageId);
	}

	public PMConfiguration facebook(FacebookConfig config) {
		this.facebook().put(config.getPageId(), config);
		return this;
	}

	// TWITTER
	public Map<String, TwitterConfig> twitter() {
		if (ArgUtil.isEmpty(twitter)) {
			twitter = new HashMap<String, TwitterConfig>();
		}
		return twitter;
	}

	public TwitterConfig twitter(String handler) {
		return twitter().get(handler);
	}

	public PMConfiguration twitter(TwitterConfig config) {
		this.twitter().put(config.getHandler(), config);
		return this;
	}

	public Map<String, TwitterConfig> getTwitter() {
		return twitter;
	}

	public void setTwitter(Map<String, TwitterConfig> twitter) {
		this.twitter = twitter;
	}

	// Telegram
	public Map<String, TelegramConfig> telegram() {
		if (ArgUtil.isEmpty(telegram)) {
			telegram = new HashMap<String, TelegramConfig>();
		}
		return telegram;
	}

	public Map<String, TelegramConfig> getTelegram() {
		return telegram;
	}

	public void setTelegram(Map<String, TelegramConfig> telegram) {
		this.telegram = telegram;
	}

	public PMConfiguration telegram(TelegramConfig config) {
		this.telegram().put(config.getHandler(), config);
		return this;
	}

	public TelegramConfig telegram(String handler) {
		return telegram().get(handler);
	}

	// GupShup
	public Map<String, GupShupConfig> gupshup() {
		if (ArgUtil.isEmpty(gupshup)) {
			gupshup = new HashMap<String, GupShupConfig>();
		}
		return gupshup;
	}

	public Map<String, GupShupConfig> getGupshup() {
		return gupshup;
	}

	public void setGupshup(Map<String, GupShupConfig> gupshup) {
		this.gupshup = gupshup;
	}

	public GupShupConfig gupshup(String handler) {
		return gupshup().get(handler);
	}

	public PMConfiguration gupshup(GupShupConfig config) {
		this.gupshup().put(config.getNumber(), config);
		return this;
	}

	// Agent
	public AgentConfig agent() {
		if (ArgUtil.isEmpty(agent)) {
			agent = new AgentConfig();
		}
		return agent;
	}

	public AgentConfig getAgent() {
		return agent;
	}

	public void setAgent(AgentConfig agent) {
		this.agent = agent;
	}

	public PMConfiguration agent(AgentConfig agent) {
		this.agent = agent;
		return this;
	}
}
