package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.common.impl.ConfigMeta;
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
	    // PRefixe
	    "mry.prop.logo.", "mry.prop.service.", "mry.prop.social.", };

    private Map<String, String> PUBLIC_CONFIG = new ConcurrentHashMap<String, String>();

    @Autowired
    private PMClientConfig chatClientConfig;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private AppConfig appConfig;

    @Value("${mry.cdn.url}")
    private String cdnUrl;

    @Value("${mry.app.login.secret}")
    private String appLoginSecret;

    @Value("${common.const.app}")
    private String app;

    @Autowired
    CDNBuilder cdnBuilder;

    @Autowired
    private Environment environment;

    public String getCdnServer() {
	return cdnBuilder.latest(pmEnvironment.get("mry.cdn.url").asString(cdnUrl));
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
	for (ConfigMeta config : ConfigManager.CONFIG_LIST) {
	    setup.put(config.getKey().toUpperCase(), pmEnvironment.get(config.getKey()).getValue());
	}
	map.put("SETUP", setup);
	map.put("timestamp", System.currentTimeMillis());

	for (Entry<String, String> entry : PUBLIC_CONFIG.entrySet()) {
	    // String newKey = entry.getValue().replaceAll("[\\.@\\-$]", "_").toUpperCase();
	    map.put(entry.getValue(), pmEnvironment.get(entry.getKey()).asString());
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

    @Autowired
    private PMEnvironment pmEnvironment;

    @SuppressWarnings("rawtypes")
    @PostConstruct
    public void init() {
	for (org.springframework.core.env.PropertySource<?> propertySource : ((ConfigurableEnvironment) environment)
		.getPropertySources()) {
	    if (propertySource instanceof EnumerablePropertySource) {
		for (String key : ((EnumerablePropertySource) propertySource).getPropertyNames()) {
		    for (String prefix : PROPS) {
			if (key.startsWith(prefix)) {
			    String shortKey = key.replace("mry.prop.", "");
			    String newKey = shortKey.replaceAll("[\\.@\\-$]", "_").toUpperCase();
			    PUBLIC_CONFIG.put(key, "PROP_" + newKey);
			}
		    }
		}
	    }
	}

    }

    public String getAppLoginSecret() {
	return appLoginSecret;
    }

}
