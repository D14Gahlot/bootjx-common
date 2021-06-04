package com.boot.jx.bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMEnvironment;
import com.boot.utils.ArgUtil;

@Component
public class BotSessionBean implements AuditDetailProvider {

	private static final long serialVersionUID = 26049494178384497L;

	@Autowired
	private PMEnvironment environment;

	@Value("${postman.default.sender}")
	private String defaultSender;

	@Override
	public String getAuditUser() {
		return ArgUtil.parseAsString(environment.config().agent().getDefaultBotName(), defaultSender);
	}

}
