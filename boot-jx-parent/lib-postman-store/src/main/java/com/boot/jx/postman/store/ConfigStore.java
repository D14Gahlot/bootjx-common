package com.boot.jx.postman.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ChannelConfigDupsDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.PermsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc.CompanyTokenKeyDoc;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.EntityDtoUtil;
import com.mongodb.client.result.DeleteResult;

@Component
public class ConfigStore extends CommonMongoTemplateAbstract<ConfigStore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigStore.class);

	public void saveConfiguration(PMConfigurationDoc doc) {
		doc.getAccountKey(); // Populate Keys of not exists
		save(doc);
	}

	public void savePrefsConfig(PrefsConfigDoc prefsConfigDoc) {
		save(prefsConfigDoc);
		log(prefsConfigDoc, "updated");
	}

	public void savePermConfig(PermsConfigDoc prefsConfigDoc) {
		save(prefsConfigDoc);
		log(prefsConfigDoc, "updated");
	}

	public void saveChannelConfig(ChannelConfigDoc configDoc, String action) {
		if ("disable".equalsIgnoreCase(action)) {
			configDoc.disabled(true);
		} else if ("enable".equalsIgnoreCase(action)) {
			configDoc.disabled(false);
		} else if ("sandbox_enable".equalsIgnoreCase(action)) {
			configDoc.setSandbox(true);
		} else if ("sandbox_disable".equalsIgnoreCase(action)) {
			configDoc.setSandbox(false);
		} else if ("shared_enable".equalsIgnoreCase(action)) {
			configDoc.setShared(true);
		} else if ("shared_disable".equalsIgnoreCase(action)) {
			configDoc.setShared(false);
		}
		configDoc.getChannelKey(); // Populate Keys of not exists
		save(configDoc);
		log(configDoc, "updated", action);
	}

	public void saveChannelConfig(ChannelConfigDoc doc) {
		saveChannelConfig(doc, null);
	}

	public void saveClientKeyConfig(ClientAppConfigDoc clientApiKey) {
		try {
			boolean generated = false;
			if (!ArgUtil.is(clientApiKey.getId()) || !ArgUtil.is(clientApiKey.getKey())) {
				clientApiKey.setKey(PostManUtil.UNIQUE_API_KEY());
				generated = true;
			} else {
				ClientAppConfigDoc oldDoc = findByIdString(clientApiKey.getId(), ClientAppConfigDoc.class);
				clientApiKey.setKey(oldDoc.getKey());
			}

			clientApiKey.setAppHook(String.format("https://{{domain}}.{{server}}/bot/ext/app/%s/{{id}}/%s",
					clientApiKey.getQueue(), CryptoUtil.getMD5Hash(clientApiKey.getKey())));

			save(clientApiKey);
			log(clientApiKey, "updated");
			if (generated == false) {
				clientApiKey.setKey("");
			}
		} catch (org.springframework.dao.DuplicateKeyException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error("saveClientKeyConfig", e);
		}
	}

	public DeleteResult remove(Object object) {
		DeleteResult r = super.remove(object);
		log(object, "deleted");
		return r;
	}

	public void saveCompanyVar(VarsConfigDoc refreshableConfigDoc) {
		try {

			VarsConfigDoc companyVarOld = findById(refreshableConfigDoc.getId(), refreshableConfigDoc.getClass());
			if (ArgUtil.is(companyVarOld)) {
				if (refreshableConfigDoc.getValue() == null) {
					refreshableConfigDoc.setValue(companyVarOld.getValue());
				}

				if (refreshableConfigDoc instanceof CompanyTokenKeyDoc) {
					CompanyTokenKeyDoc companyTokenKeyDoc = (CompanyTokenKeyDoc) refreshableConfigDoc;
					CompanyTokenKeyDoc companyTokenKeyDocOld = (CompanyTokenKeyDoc) companyVarOld;
					companyTokenKeyDocOld.secret().putAll(companyTokenKeyDoc.secret());
					companyTokenKeyDoc.secret().putAll(companyTokenKeyDocOld.secret());
				}
			}

			save(refreshableConfigDoc);
			log(refreshableConfigDoc, "updated");
		} catch (Exception e) {
			LOGGER.error("saveClientKeyConfig", e);
		}
	}

	@Async
	public void saveMaster(ChannelConfigDoc configDoc) {
		ChannelConfigDupsDoc masterDoc = EntityDtoUtil.dtoToEntity(configDoc, new ChannelConfigDupsDoc());
		masterDoc.setId(masterDoc.getDomain() + ":" + masterDoc.getId());
		masterDoc.setChannelId(configDoc.getChannelId());
		AppContextUtil.clear();
		AppContextUtil.setTenant(Tenants.getDefault());
		AppContextUtil.init();
		save(masterDoc);
	}

	@Async
	public void saveMaster(PrefsConfigDoc prefsConfigDoc) {
		PrefsConfigDoc masterDoc = EntityDtoUtil.dtoToEntity(prefsConfigDoc, new PrefsConfigDoc());
		masterDoc.setId(masterDoc.getDomain() + ":" + masterDoc.getId());
		AppContextUtil.clear();
		AppContextUtil.setTenant(Tenants.getDefault());
		AppContextUtil.init();
		save(masterDoc, "DUPS_CONFIG_PREFS");
	}

	@Async
	public void saveMaster(PermsConfigDoc permConfigDoc) {
		PermsConfigDoc masterDoc = EntityDtoUtil.dtoToEntity(permConfigDoc, new PermsConfigDoc());
		masterDoc.setId(masterDoc.getDomain() + ":" + masterDoc.getId());
		AppContextUtil.clear();
		AppContextUtil.setTenant(Tenants.getDefault());
		AppContextUtil.init();
		save(masterDoc, "DUPS_CONFIG_PERMS");
	}

}
