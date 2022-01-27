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
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.TimeUtils;

@Component
@PropertySource("classpath:application-common.properties")
public class AppCommonConfigImpl implements AppCommonConfig {

    private static final Logger LOGGER = LoggerService.getLogger(AppCommonConfigImpl.class);

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

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

    private SafeKeyHashMap<Object> setupConfigAttributes() {
	SafeKeyHashMap<Object> setup = new SafeKeyHashMap<Object>();
	for (ConfigMeta config : ConfigConstants.SETUP_CONFIG_LIST) {
	    setup.put(config.getKey().toUpperCase(), pmEnvironment.keyEntry(config.getKey()).getValue());
	}
	return setup;
    }

    private Map<String, Object> commonAttributes() {
	Map<String, Object> map = new HashMap<String, Object>();
	map.put("AGENT_CHAT_INIT", pmEnvironment.keyEntry("postman.agent.chat.init").asBoolean());
	map.put("CHAT_TAG_ENABLED", pmEnvironment.local().keyEntry("chat.tag.enabled").asBoolean());
	map.put("chatIdleTimeout", TimeUtils.toMillis(chatClientConfig.getChatIdleTimeout()));
	map.put("agentSessionTimeout", chatClientConfig.getAgentSessionTimeout().toMillis());
	map.put("chatSessionTimeout", TimeUtils.toMillis(chatClientConfig.getChatSessionTimeout()));
	return map;
    }

    public Map<String, Object> configAttributes() {
	Map<String, Object> map = commonAttributes();
	map.putAll(appConfigAttributes());
	map.put("SETUP", setupConfigAttributes());
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
	map.put("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), getCdnServer()));
	map.put("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
	map.put("CDN_VERSION", "V3");
	map.put("CDN_VERSION", getVersion());

	map.put("APP_CONTEXT", appConfig.getAppPrefix());
	map.put("POSTMAN_CONTEXT", appConfig.getAppPrefix());

	map.put("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.keyEntry("postman.agent.scheme.color").asString());
	map.put("STAMP", System.currentTimeMillis());
	map.put("APP_TITLE", appConfig.getAppTitle());
	map.put("TENANT", AppContextUtil.getTenant());

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

}
