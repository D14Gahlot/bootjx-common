package com.boot.jx.postman.store;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfigPackage.AppSharedConfigChange;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDupsDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.PermsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mongodb.client.result.DeleteResult;

@Component
public class ConfigMaster extends CommonMongoTemplateAbstract<ConfigMaster> {

	private Cache<String, List<ChannelConfigDupsDoc>> channelList = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(1, TimeUnit.HOURS).build();

	@Autowired(required = false)
	private ConfigStore configStore;

	public void saveChannelConfig(ChannelConfigDoc configDoc) {
		configStore.saveChannelConfig(configDoc);
		configStore.saveMaster(configDoc);
	}

	public void saveChannelConfig(ChannelConfigDoc configDoc, String action) {
		configStore.saveChannelConfig(configDoc, action);
		configStore.saveMaster(configDoc);
	}

	public DeleteResult remove(ChannelConfigDoc configDoc) {
		DeleteResult r = configStore.remove(configDoc);
		configDoc.setDeleted(true);
		configDoc.setDisabled(true);
		configStore.saveMaster(configDoc);
		return r;
	}

	public void savePrefsConfig(PrefsConfigDoc prefsConfigDoc) {
		configStore.savePrefsConfig(prefsConfigDoc);
		configStore.saveMaster(prefsConfigDoc);
	}

	public void saveConfiguration(PMConfigurationDoc doc) {
		configStore.saveConfiguration(doc);
	}

	public void saveClientKeyConfig(ClientAppConfigDoc clientApiKey) {
		configStore.saveClientKeyConfig(clientApiKey);
	}

	public <T extends VarsConfigDoc> void saveCompanyVar(T companyVarsConfig) {
		configStore.saveCompanyVar(companyVarsConfig);
	}

	public void savePermConfig(PermsConfigDoc permConfigDoc) {
		configStore.savePermConfig(permConfigDoc);
		configStore.saveMaster(permConfigDoc);
	}

	public List<ChannelConfigDupsDoc> getChannelMeta(String channelType, String lane) {
		String channelId = PostManUtil.CHANNEL_ID(channelType, lane);
		List<ChannelConfigDupsDoc> channels = channelList.getIfPresent(channelId);
		if (!ArgUtil.is(channels) || channels.size() < 1) {
			channels = configStore.find(MQB.collection(ChannelConfigDupsDoc.class).where(Criteria.where("lane").is(lane)
					.and("isDisabled").is(false).and("isDeleted").is(false).and("channelType").is(channelType)));
			if (ArgUtil.is(channels)) {
				channelList.put(channelId, channels);
			}
		}
		return channels;
	}

	public void clearChannelMeta(AppSharedConfigChange change) {
		if (ArgUtil.is(change.getConfigType(), ChannelConfigDoc.DOCUMENT_NAME)) {
			channelList.invalidate(change.getConfigId());
		}
	}

}
