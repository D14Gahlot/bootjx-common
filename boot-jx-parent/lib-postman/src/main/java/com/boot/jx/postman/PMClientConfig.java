package com.boot.jx.postman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
@PropertySource("classpath:application-postman.properties")
public class PMClientConfig {

    public static class PATH {
	public static final String ASSIGN_TO_AGENT = "/int/assign/agent";
    }

    public static class PROPERTIES {
	private static final String POSTMAN_CHAT_SESSION_TIMEOUT = "postman.chat.session.timeout";
    }

    @Value("${postman.app.type}")
    private String postmanType;

    @Value("${postman.agent.url}")
    private String agentUrl;

    @Value("${postman.inbound.forward.url}")
    private String inboundForwardUrl;

    @Value("${postman.contact.details.url}")
    private String contactDetailsUrl;

    @Value("${postman.chat.dummy.user.enabled}")
    boolean chatDummyUserEnabled;

    @Value("${postman.chat.idle.timeout}")
    private String chatIdleTimeout;

    @Value("${" + PROPERTIES.POSTMAN_CHAT_SESSION_TIMEOUT + "}")
    private String chatSessionTimeout;

    @Value("${postman.agent.session.timeout}")
    private String agentSessionTimeout;

    @Value("${postman.default.sender}")
    private String defaultSender;

    @Autowired
    private PMEnvironment environment;

    public boolean isChatDummyUserEnabled() {
	return chatDummyUserEnabled;
    }

    public String getAgentUrl() {
	return agentUrl;
    }

    public String getDefaultSender() {
	return environment.get("postman.bot.name")
		.asString(ArgUtil.parseAsString(environment.config().agent().getDefaultBotName(), defaultSender));
    }

    public String getContactDetailsUrl() {
	return environment.config().get("postman.contact.details.url").asString(contactDetailsUrl);
    }

    public String getChatIdleTimeout() {
	return environment.config().get("postman.chat.idle.timeout").asString(chatIdleTimeout);
    }

    public String getInboundForwardUrl() {
	return inboundForwardUrl;
    }

    public void setInboundForwardUrl(String inboundForwardUrl) {
	this.inboundForwardUrl = inboundForwardUrl;
    }

    public String getPostmanType() {
	return postmanType;
    }

    public String getChatSessionTimeout() {
	return environment.config().get(PROPERTIES.POSTMAN_CHAT_SESSION_TIMEOUT).asString(chatSessionTimeout);
    }

    public String getAgentSessionTimeout() {
	return agentSessionTimeout;
    }

    private String getWebhookBase(ChannelConfig channelConfig) {
	String webhookUrl = channelConfig.getWebhookUrl();
	if (!ArgUtil.is(webhookUrl)) {
	    webhookUrl = String.format("https://%s.%s/postman", AppContextUtil.getTenant(),
		    environment.get("mry.prop.service.domain").asString());
	}
	return webhookUrl;
    }

    public String getWebhookUrl(ChannelConfig channelConfig) {
	PMConfiguration config = environment.config();
	String webhookUrl = getWebhookBase(channelConfig);
	return String.format("%s/%s", webhookUrl,
		PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelConfig));
    }

}
