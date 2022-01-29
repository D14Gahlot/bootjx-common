package com.boot.jx.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.inbound.InBound.InBoundHandler;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;

@Component
public class BotInBoundHandler implements InBoundHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotInBoundHandler.class);

    @Autowired
    public PMEnvironment pmEnvironment;

    @Autowired
    public PMDomainConfig pmDomainConfig;

    @Autowired
    public PMCommonConfig pmCommonConfig;

    @Autowired
    private BotEngine botEngine;

    @Override
    public void handle(InboxMessage inboxMessage) {
	botEngine.invokeMethodsAsync(inboxMessage);
    }

    @Override
    public void handle(MessageReport messageReport) {
	LOGGER.debug("No Handling Required for Status on BotSide");
    }

}
