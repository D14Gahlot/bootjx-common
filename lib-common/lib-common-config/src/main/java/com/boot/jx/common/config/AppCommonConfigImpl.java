package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMEnvironment;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.TimeUtils;

@Component
@PropertySource("classpath:application-common.properties")
public class AppCommonConfigImpl implements AppCommonConfig {

    public static final String[] PROPS = new String[] {
	    // LOGO Transparent
	    "logo.bg-x-logo-w", "logo.bg-x-logo-b",
	    // LOGO - WHITE
	    "logo.bg-w-logo", "logo.bg-w-logo-b",
	    // LOGO - black
	    "logo.bg-b-logo-w",
	    // ICONS
	    "logo.bg-x-icon-w", "logo.bg-x-icon",

	    // WEBSITES
	    "mry.prop.service.name", "mry.prop.service.website", "mry.prop.service.website.link",
	    "mry.prop.service.aboutus.link", "mry.prop.service.privac.link", "mry.prop.service.tos.link"

    };

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private AppConfig appConfig;

    @Value("${mry.cdn.url}")
    private String cdnUrl;

    @Value("${common.const.app}")
    private String app;

    public String getCdnServer() {
	return pmEnvironment.get("mry.cdn.url").asString(cdnUrl);
    }

    private long getVersion() {
	return System.currentTimeMillis() / 300000;
    }

    public Map<String, Object> toMap() {
	Map<String, Object> map = new HashMap<String, Object>();

	map.put("AGENT_CHAT_INIT", pmEnvironment.get("postman.agent.chat.init").asBoolean());
	map.put("CHAT_TAG_ENABLED", pmEnvironment.config().get("chat.tag.enabled").asBoolean());
	map.put("chatIdleTimeout", TimeUtils.toMillis(chatClientConfig.getChatIdleTimeout()));
	map.put("agentSessionTimeout", TimeUtils.toMillis(chatClientConfig.getAgentSessionTimeout()));
	map.put("chatSessionTimeout", TimeUtils.toMillis(chatClientConfig.getChatSessionTimeout()));

	SafeKeyHashMap<Object> setup = new SafeKeyHashMap<Object>();
	for (ConfigBuilder config : ConfigBuilder.LIST) {
	    setup.put(config.getKey().toUpperCase(), pmEnvironment.get(config.getKey()).getValue());
	}
	map.put("SETUP", setup);
	map.put("timestamp", System.currentTimeMillis());

	for (String key : PROPS) {
	    String newKey = key.replaceAll("[\\.@\\-$]", "_").toUpperCase();
	    map.put("PROP_" + newKey, pmEnvironment.get("mry.prop." + key).asString());
	}

	return map;
    }

    @Override
    public Map<String, Object> appAttributes() {
	Map<String, Object> map = new HashMap<String, Object>();

	Map<String, Object> config = toMap();
	map.put("CONFIG", config);
	map.put("CONFIG_JSON", JsonUtil.toJson(config));
	map.put("APP", app);
	map.put("CDN_URL", ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), getCdnServer()));
	map.put("CDN_DEBUG", ArgUtil.parseAsString(commonHttpRequest.get("CDN_DEBUG"), "false"));
	map.put("CDN_VERSION", "V3");
	map.put("CDN_VERSION", getVersion());

	map.put("APP_CONTEXT", appConfig.getAppPrefix());
	map.put("POSTMAN_CONTEXT", appConfig.getAppPrefix());

	map.put("POSTMAN_AGENT_SCHEME_COLOR", pmEnvironment.get("postman.agent.scheme.color").asString());
	map.put("STAMP", System.currentTimeMillis());
	map.put("APP_TITLE", appConfig.getAppTitle());

	return map;
    }

}
