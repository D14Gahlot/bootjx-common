package com.boot.jx.postman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils.TimePeriod;

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

    @Value("${postman.inbound.forward.url}")
    private String inboundForwardUrl;

    @Value("${postman.contact.details.url}")
    private String contactDetailsUrl;

    @Value("${app.local.dummy.bot.enabled}")
    boolean localDummyBotEnabled;

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

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private AppConfig appConfig;

    public boolean isLocalDummyBotEnabled() {
	return localDummyBotEnabled;
    }

    public String getDefaultSender() {
	return environment.keyEntry("postman.bot.name")
		.asString(ArgUtil.parseAsString(environment.local().agent().getDefaultBotName(), defaultSender));
    }

    public String getContactDetailsUrl() {
	return environment.local().keyEntry("postman.contact.details.url").asString(contactDetailsUrl);
    }

    public String getChatIdleTimeout() {
	return environment.local().keyEntry("postman.chat.idle.timeout").asString(chatIdleTimeout);
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
	return environment.local().keyEntry(PROPERTIES.POSTMAN_CHAT_SESSION_TIMEOUT).asString(chatSessionTimeout);
    }

    public TimePeriod getAgentSessionTimeout() {
	return TimePeriod.from(agentSessionTimeout);
    }

    public String getWebhookBase(ChannelConfig channelConfig) {
	String webhookUrl = channelConfig.getWebhookUrl();
	if (!ArgUtil.is(webhookUrl)) {
	    if (isLocalDummyBotEnabled()) {
		webhookUrl = String.format("%s%s", commonHttpRequest.getServerHost(), appConfig.getAppPrefix(),
			environment.keyEntry("mry.prop.service.domain").asString());
	    } else {
		webhookUrl = String.format("https://%s.%s/postman", AppContextUtil.getTenant(),
			environment.keyEntry("mry.prop.service.domain").asString());
	    }
	}
	return webhookUrl;
    }

    public String getWebhookUrl(ChannelConfig channelConfig) {
	PMConfigurationModel config = environment.local();
	String webhookUrl = getWebhookBase(channelConfig);
	return String.format("%s/%s", webhookUrl,
		PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelConfig));
    }

}
