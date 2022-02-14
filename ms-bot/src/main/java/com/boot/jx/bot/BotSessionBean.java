package com.boot.jx.bot;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;

@Component
public class BotSessionBean implements AuditDetailProvider, Serializable {

    private static final long serialVersionUID = 26049494178384497L;

    @Autowired
    PMClientConfig pmClientConfig;

    @Override
    public String getAuditUser() {
	return pmClientConfig.getDefaultSender();
    }

}
