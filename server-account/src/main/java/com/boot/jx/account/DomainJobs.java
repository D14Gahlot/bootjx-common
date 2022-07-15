package com.boot.jx.account;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.dict.ContactType;
import com.boot.jx.inbound.InBoundPoller;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQB.CommonMongoQBimpl;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@EnableScheduling
@Component
public class DomainJobs {

	private static final Logger LOGGER = LoggerService.getLogger(DomainJobs.class);

	@Autowired
	AppConfig appConfig;

	@Autowired
	InBoundPoller inBoundPoller;

	@Autowired
	AccountStore accountStore;

	@Autowired
	PMEnvironment pmEnvironment;

	@Scheduled(fixedDelay = 5000, initialDelay = 60000)
	public void fetchEmailTask() throws InterruptedException {
		// LOGGER.info("======= I am doing my Task @ {}", appConfig.getSpringAppName());
		AppContextUtil.clear();
		AppContextUtil.setTenant("app");
		AppContextUtil.init();
		LOGGER.debug("Searching Domains");
		if(true) {
			//return;
		}

		String serviceDomain = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString();

		List<DomainDoc> domainDocs = accountStore.findAllDomainByServer(serviceDomain);
		CommonMongoQBimpl<ChannelConfigDoc> emailChannelsQuery = CommonMongoQueryBuilder
				.collection(ChannelConfigDoc.class).where("contactType", ContactType.EMAIL.name());

		for (DomainDoc domainDoc : domainDocs) {
			AppContextUtil.clear();
			AppContextUtil.setTenant(domainDoc.getDomain());
			AppContextUtil.init();
			LOGGER.debug("Searching Config {}", domainDoc.getDomain());
			List<ChannelConfigDoc> emailChannels = accountStore.find(emailChannelsQuery);

			for (ChannelConfigDoc emailChannel : emailChannels) {
				if (!emailChannel.isDisabled()) {
					if (ArgUtil.is(emailChannels) && emailChannels.size() > 0) {
						LOGGER.info("Found Config {} ---> {}", domainDoc.getDomain(), emailChannel.getChannelId());
						inBoundPoller.doTask(new TunnelTask().name(InBoundPoller.TASK_EMAIL_POLLER)
								.id(domainDoc.getDomain() + "_" + emailChannel.getChannelId()).intervalSeconds(15)
								.data(MapModel.createInstance().put("channelId", emailChannel.getChannelId())));
					}
				}
			}
			AppContextUtil.clear();
		}
	}

}
