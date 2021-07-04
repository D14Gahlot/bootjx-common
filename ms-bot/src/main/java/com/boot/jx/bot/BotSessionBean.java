package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment;

@Component
public class BotSessionBean implements AuditDetailProvider {

	private static final long serialVersionUID = 26049494178384497L;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	PMClientConfig pmClientConfig;

	@Override
	public String getAuditUser() {
		return pmClientConfig.getDefaultSender();
	}

}
