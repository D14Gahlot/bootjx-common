package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppSharedConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.ChannelConfig;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment.PMEnvironmentProvider;
import com.boot.jx.postman.doc.ChannelConfigDoc;
import com.boot.jx.postman.doc.ClientApiKeyDoc;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.fb.FacebookConfig;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.tg.TelegramConfig;
import com.boot.jx.postman.tw.TwitterConfig;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.StringUtils;

@Component
public class PMEnvironmentProviderImpl implements PMEnvironmentProvider, AppSharedConfig {

    private Map<String, PMConfigurationDoc> connectors = new HashMap<String, PMConfigurationDoc>();

    PMConfigurationDoc sharedConfiguration = null;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public PMConfiguration config() {
	String tnt = AppContextUtil.getTenant();
	if (connectors.containsKey(tnt)) {
	    return connectors.get(tnt);
	}
	if (ArgUtil.is(mongoTemplate)) {
	    PMConfigurationDoc x = getPMConfigurationDoc();

	    List<ChannelConfigDoc> channels = mongoTemplate.findAll(ChannelConfigDoc.class);

	    for (ChannelConfigDoc channel : channels) {
		x.channels(channel);
	    }

	    List<ClientApiKeyDoc> clientKeys = mongoTemplate.findAll(ClientApiKeyDoc.class);
	    for (ClientApiKeyDoc clientKey : clientKeys) {
		x.clientApiKey(clientKey);
	    }

	    if (ArgUtil.is(x)) {
		connectors.put(tnt, x);
	    }

	    if (Tenants.isDefault(tnt)) {
		PMConfigurationDoc newSharedConfiguration = new PMConfigurationDoc();
		for (Entry<String, PMConfigurationObject> entry : x.map().entrySet()) {
		    if (entry.getValue().isShared()) {
			newSharedConfiguration.set(entry.getValue());
		    }
		}
		sharedConfiguration = newSharedConfiguration;
	    }

	    return x;
	}
	return null;
    }

    public void config(PMConfiguration config) {

	if (ArgUtil.is(mongoTemplate)) {

	    if (ArgUtil.is(config) && ArgUtil.is(config.getFacebook())) {
		for (Entry<String, FacebookConfig> configEntry : config.getFacebook().entrySet()) {
		    configInternal(new ChannelConfigDoc().from(configEntry.getValue()));
		}
	    }

	    if (ArgUtil.is(config) && ArgUtil.is(config.getGupshup())) {
		for (Entry<String, GupShupConfig> configEntry : config.getGupshup().entrySet()) {
		    configInternal(new ChannelConfigDoc().from(configEntry.getValue()));
		}
	    }

	    if (ArgUtil.is(config) && ArgUtil.is(config.getTwitter())) {
		for (Entry<String, TwitterConfig> configEntry : config.getTwitter().entrySet()) {
		    configInternal(new ChannelConfigDoc().from(configEntry.getValue()));
		}
	    }

	    if (ArgUtil.is(config) && ArgUtil.is(config.getTelegram())) {
		for (Entry<String, TelegramConfig> configEntry : config.getTelegram().entrySet()) {
		    configInternal(new ChannelConfigDoc().from(configEntry.getValue()));
		}
	    }

	    PMConfigurationDoc doc = EntityDtoUtil.dtoToEntity(config, new PMConfigurationDoc());
	    doc.setTenant(AppContextUtil.getTenant());
	    mongoTemplate.save(doc);
	}

    }

    public void configInternal(ChannelConfig config) {
	ChannelConfigDoc doc = EntityDtoUtil.dtoToEntity(config, new ChannelConfigDoc());
	doc.setId(StringUtils.toLowerCase(doc.getChannelId()));
	if (config.isDisabled()) {
	    mongoTemplate.remove(doc);
	} else
	    mongoTemplate.save(doc);
    }

    @Override
    public void config(ChannelConfig config) {
	configInternal(config);
	PMConfigurationDoc doc = getPMConfigurationDoc();
	switch (config.getChannelType()) {
	case CHANNEL_TYPE.FACEBOOK:
	    doc.facebook(config.getFacebook(), config.isDisabled());
	    break;
	case CHANNEL_TYPE.GUPSHUP:
	    doc.gupshup(config.getGupshup(), config.isDisabled());
	    break;
	case CHANNEL_TYPE.TELEGRAM:
	    doc.telegram(config.getTelegram(), config.isDisabled());
	    break;
	case CHANNEL_TYPE.TWITTER:
	    doc.twitter(config.getTwitter(), config.isDisabled());
	    break;
	default:
	}
	mongoTemplate.save(doc);
    }

    private PMConfigurationDoc getPMConfigurationDoc() {
	PMConfigurationDoc doc = mongoTemplate.findById(AppContextUtil.getTenant(), PMConfigurationDoc.class);
	if (ArgUtil.isEmpty(doc)) {
	    doc = new PMConfigurationDoc();
	    doc.setTenant(AppContextUtil.getTenant());
	}
	return doc;
    }

    @Override
    public void clear(Map<String, String> map) {
	String tnt = AppContextUtil.getTenant();
	connectors.remove(tnt);
    }

    @Override
    public PMConfiguration shared() {
	return sharedConfiguration;
    }

}
