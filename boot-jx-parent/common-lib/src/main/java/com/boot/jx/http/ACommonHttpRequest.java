package com.boot.jx.http;

import java.net.MalformedURLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.boot.utils.HttpUtils;
import com.boot.utils.Urly;

public abstract class ACommonHttpRequest {

    public abstract HttpServletRequest getRequest();

    public abstract HttpServletResponse getResponse();

    /**
     * 
     * @see HttpServletRequest#getRequestURI()
     * @return
     */
    public String getRequestURI() {
	return getRequest().getRequestURI();
    }

    public String getServerName() {
	return HttpUtils.getServerName(getRequest());
    }

    public String getSubDomain() {
	try {
	    return Urly.getSubDomainName(getRequest().getServerName());
	} catch (MalformedURLException e) {
	    return null;
	}
    }
}
