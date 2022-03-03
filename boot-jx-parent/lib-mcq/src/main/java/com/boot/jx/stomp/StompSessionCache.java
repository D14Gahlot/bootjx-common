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

	String xsessionId;
	String jsessionId;
	String prefix;
	String[] tags;
	String tenantToken;

	public String getPrefix() {
	    return prefix;
	}

	public void setPrefix(String prefix) {
	    this.prefix = prefix;
	}

	public String getXsessionId() {
	    return xsessionId;
	}

	public void setXsessionId(String xSessionId) {
	    this.xsessionId = xSessionId;
	}

	public String[] getTags() {
	    return tags;
	}

	public void setTags(String[] tags) {
	    this.tags = tags;
	}

	public String getTenantToken() {
	    return tenantToken;
	}

	public void setTenantToken(String tenantToken) {
	    this.tenantToken = tenantToken;
	}

	public String getJsessionId() {
	    return jsessionId;
	}

	public void setJsessionId(String jsessionId) {
	    this.jsessionId = jsessionId;
	}

    }

    /**
     * Instantiates a new logged in users.
     */
    public StompSessionCache() {
	super(StompSession.class.getName() + "V4", 4);
    }

}
