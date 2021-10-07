package com.boot.jx.postman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMConstants.DEFAULT;

@Component
public class PostManSessionBean implements AuditDetailProvider {

	private static final long serialVersionUID = 26049494178384497L;

	@Autowired
	private PMEnvironment environment;

	@Override
	public String getAuditUser() {
		return DEFAULT.SYSTEM;
	}

}
