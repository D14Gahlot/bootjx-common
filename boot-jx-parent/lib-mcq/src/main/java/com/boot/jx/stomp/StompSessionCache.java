package com.boot.jx.stomp;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.boot.jx.cache.CacheBox;
import com.boot.jx.stomp.StompSessionCache.StompSession;

/**
 * The Class StompActiveUsers.
 */
@Component
public class StompSessionCache extends CacheBox<StompSession> {

	public static class StompSession implements Serializable {

		private static final long serialVersionUID = 2457062425857422747L;

		String httpSessionId;
		String prefix;
		String[] tags;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getHttpSessionId() {
			return httpSessionId;
		}

		public void setHttpSessionId(String httpSessionId) {
			this.httpSessionId = httpSessionId;
		}

		public String[] getTags() {
			return tags;
		}

		public void setTags(String[] tags) {
			this.tags = tags;
		}

	}

	/**
	 * Instantiates a new logged in users.
	 */
	public StompSessionCache() {
		super(StompSession.class.getName() + "V2");
	}

}
