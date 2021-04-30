package com.boot.jx.postman;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;
import com.boot.utils.ArgUtil;

public class ConnectorConfig implements Serializable {

	private static final long serialVersionUID = -5432956433673368768L;

	Map<String, FacebookConfig> facebook;
	Map<String, TwitterConfig> twitter;
	Map<String, TelegramConfig> telegram;

	// Facebook
	public Map<String, FacebookConfig> getFacebook() {
		return facebook;
	}

	public void setFacebook(Map<String, FacebookConfig> facebook) {
		this.facebook = facebook;
	}

	public FacebookConfig facebook(String pageId) {
		return facebook.get(pageId);
	}

	public Map<String, FacebookConfig> facebook() {
		if (ArgUtil.isEmpty(facebook)) {
			facebook = new HashMap<String, FacebookConfig>();
		}
		return facebook;
	}

	public ConnectorConfig facebook(FacebookConfig config) {
		this.facebook().put(config.getPageId(), config);
		return this;
	}

	// TWITTER
	public TwitterConfig twitter(String handler) {
		return twitter.get(handler);
	}

	public Map<String, TwitterConfig> twitter() {
		if (ArgUtil.isEmpty(twitter)) {
			twitter = new HashMap<String, TwitterConfig>();
		}
		return twitter;
	}

	public ConnectorConfig twitter(TwitterConfig config) {
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
	public Map<String, TelegramConfig> getTelegram() {
		return telegram;
	}

	public void setTelegram(Map<String, TelegramConfig> telegram) {
		this.telegram = telegram;
	}

	public TelegramConfig telegram(String handler) {
		return telegram.get(handler);
	}

	public Map<String, TelegramConfig> telegram() {
		if (ArgUtil.isEmpty(telegram)) {
			telegram = new HashMap<String, TelegramConfig>();
		}
		return telegram;
	}

	public ConnectorConfig telegram(TelegramConfig config) {
		this.telegram().put(config.getHandler(), config);
		return this;
	}

}
