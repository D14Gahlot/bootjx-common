package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.common.config.ConfigConstants.PERMS_KEY;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.TimeUtils;
import com.boot.utils.UniqueID;

@Component
@PropertySource("classpath:application-common.properties")
public class PMCommonConfigImpl implements PMCommonConfig {

	private static final Logger LOGGER = LoggerService.getLogger(PMCommonConfigImpl.class);

	@Autowired
	private PMClientConfig chatClientConfig;

	@Autowired
	private PMDomainConfig pmDomainConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private TenantResolver tenantResolver;

	@Autowired
	private AppConfig appConfig;

	@Value("${mry.duperadmin.email}")
	private String duperEmail;

	@Value("${mry.cdn.url}")
	private String cdnUrl;

	@Value("${mry.app.login.secret}")
	private String appLoginSecret;

	@Value("${common.const.app}")
	private String app;

	@Value("${mry.bot.url}")
	private String botUrl;

	@Value("${mry.agent.url}")
	private String agentUrl;

	@Value("${mry.scriptus.url}")
	private String scriptusUrl;

	@Value("${mry.scriptus.secret}")
	private String scriptusSecret;

	@Value("${mry.prop.service.server}")
	private String serviceServer;

	@Autowired
	private CDNBuilder cdnBuilder;

	@Autowired
	private PMEnvironment pmEnvironment;

	public String getCdnServer() {

		boolean isBeta = ArgUtil.parseAsBoolean(commonHttpRequest.get("postman.ui.beta"),
				pmEnvironment.local().keyEntry("postman.ui.beta").asBoolean(Boolean.FALSE).booleanValue());
		if (isBeta) {
			boolean betaEnabled = pmEnvironment.keyEntry("postman.ui.beta").asBoolean();
			if (betaEnabled) {
				String betaCdn = pmEnvironment.keyEntry("mry.cdn.url.beta").asString();
				if (ArgUtil.is(betaCdn)) {
					return cdnBuilder.latest(betaCdn);
				}
			}
		}
		return cdnBuilder.latest(pmEnvironment.keyEntry("mry.cdn.url").asString(cdnUrl));
	}

	public String getCdnServerDebug() {
		String debugCdnUrl = commonHttpRequest.get("CDN_URL");
		if (ArgUtil.is(debugCdnUrl) && !(debugCdnUrl.startsWith("http://") || debugCdnUrl.startsWith("https://"))) {
			debugCdnUrl = CryptoUtil.getEncoder().message(debugCdnUrl).decodeBase64().toString();
		}
		return ArgUtil.parseAsString(debugCdnUrl, getCdnServer());
	}

	private long getVersion() {
		return System.currentTimeMillis() / 300000;
	}

	public Map<String, Object> appConfigAttributes() {
		Map<String, Object> map = new HashMap<String, Object>();
		for (Entry<String, String> entry : ConfigConstants.APP_CONFIG.entrySet()) {
			map.put(entry.getValue(), pmEnvironment.keyEntry(entry.getKey()).asString());
		}
		return map;
	}

	private SafeKeyHashMap<Object> permsConfigAttributes() {
		SafeKeyHashMap<Object> setup = new SafeKeyHashMap<Object>();
		for (PERMS_KEY config : ConfigConstants.PERMS_KEY.values()) {
			setup.put(config.name(),
					ArgUtil.nonEmpty(pmEnvironment.permEntry(config.getKey()).getValue(), config.getDefaultValue()));
		}
		return setup;
	}

	private SafeKeyHashMap<Object> setupConfigAttributes() {
		SafeKeyHashMap<Object> setup = new SafeKeyHashMap<Object>();
		for (ConfigMeta config : ConfigConstants.SETUP_CONFIG_LIST) {
			setup.put(config.getKey().toUpperCase(), pmEnvironment.keyEntry(config.getKey()).getValue());
		}

		// Default Web Channel
		PMConfigurationObject defaultWebChannel = pmEnvironment
				.keyEntry(PMConstants.PROPERTIES.POSTMAN_CHAT_WEB_CHANNEL);
		ChannelConfig channelConfig = pmEnvironment.config()
				.channel(defaultWebChannel.asString("web:" + getServiceServer()));
		if (ArgUtil.is(channelConfig)) {
			setup.put("POSTMAN_CHAT_WEB_CHANNEL", channelConfig.getChannelId());
			setup.put("POSTMAN_CHAT_WEB_CHANNEL_KEY", channelConfig.getChannelKey());
			if (ArgUtil.is(channelConfig.getWeb())) {
				if (ArgUtil.is(channelConfig.getWeb().getTitle())) {
					setup.put("POSTMAN_CHAT_WEB_CHANNEL_TITLE", channelConfig.getWeb().getTitle());
				}
				if (ArgUtil.is(channelConfig.getWeb().getStylesheet())) {
					setup.put("POSTMAN_CHAT_WEB_CHANNEL_STYLESHEET", channelConfig.getWeb().getStylesheet());
				}
			}
		}
		return setup;
	}

	private Map<String, Object> commonAttributes() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("AGENT_CHAT_INIT", pmEnvironment.keyEntry("postman.agent.chat.init").asBoolean());
		map.put("CHAT_TAG_ENABLED", pmEnvironment.local().keyEntry("chat.tag.enabled").asBoolean());
		map.put("chatIdleTimeout", pmDomainConfig.getChatIdleTimeout().asMillis());
		map.put("agentSessionTimeout", chatClientConfig.getAgentSessionTimeout().toMillis());
		map.put("chatSessionTimeout", TimeUtils.toMillis(chatClientConfig.getChatSessionTimeout()));
		return map;
	}

	public Map<String, Object> configAttributes() {
		Map<String, Object> map = commonAttributes();
		map.putAll(appConfigAttributes());
		map.put("SETUP", setupConfigAttributes());
		map.put("PERMS", permsConfigAttributes());
		map.put("timestamp", System.currentTimeMillis());
		return map;
	}

	@Override
	public Map<String, Object> appAttributes() {
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> config = configAttributes();
		map.put("CONFIG", config);
		map.put("CONFIG_JSON", JsonUtil.toJson(config));
		map.put("APP", app);
		String debugCdnUrl = getCdnServerDebug();

		map.put("CDN_URL", debugCdnUrl);

		if (ArgUtil.is(debugCdnUrl) && (debugCdnUrl.contains("127.0.0.1") || debugCdnUrl.contains("localhost"))) {
			map.put("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "true"));
		} else {
			map.put("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
		}

		map.put("CDN_VERSION", "V3");
		map.put("CDN_VERSION", getVersion());

		map.put("APP_CONTEXT", appConfig.getAppPrefix());
		map.put("POSTMAN_CONTEXT", appConfig.getAppPrefix());

		map.put("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.keyEntry("postman.agent.scheme.color").asString());
		map.put("STAMP", System.currentTimeMillis());
		map.put("APP_TITLE", appConfig.getAppTitle());
		map.put("TENANT", AppContextUtil.getTenant());
		map.put("NOUNCE", UniqueID.generateString62());
		return map;
	}

	public String getAppLoginSecret() {
		return appLoginSecret;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
		try {
			pmEnvironment.initConfig();
		} catch (Exception e) {
			LOGGER.error("pmEnvironment.reload", e);
		}
	}

	public String getDuperEmail() {
		return duperEmail;
	}

	public String getBotUrl() {
		return pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_BOT_URL).asString(this.botUrl);
	}

	@Override
	public String getAgentUrl() {
		return pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_AGENT_URL).asString(this.agentUrl);
	}

	@Override
	public boolean isValidDomain() {
		return tenantResolver.isValid();
	}

	@Override
	public boolean isDefaultDomain() {
		return Tenants.isDefault(AppContextUtil.getTenant());
	}

	@Override
	public String mainDomainRedirect() {
		return "redirect:" + String.format("https://app.%s%s",
				pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString(),
				commonHttpRequest.getRequestURI());
	}

	@Override
	public String mainDomainRedirect(String path) {
		return "redirect:" + String.format("https://app.%s/%s",
				pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString(), path);
	}

	@Override
	public String getScriptusUrl() {
		// return "http://localhost:8085/";
		return pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SCRIPTUS_URL).asString(this.scriptusUrl);
	}

	@Override
	public String getScriptusSecret() {
		return scriptusSecret;
	}

	@Override
	public String getServiceServer() {
		return pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString(serviceServer);
	}

}
