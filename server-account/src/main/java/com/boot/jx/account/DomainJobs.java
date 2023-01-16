package com.boot.jx.account;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.dict.ContactType;
import com.boot.jx.inbound.InBoundPoller;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.config.ChannelConfigDupsDoc;
import com.boot.jx.postman.store.ConfigMaster;
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
	ConfigMaster configMaster;

	@Autowired
	PMEnvironment pmEnvironment;

	@Scheduled(fixedDelay = 5000, initialDelay = 60000)
	public void fetchEmailTask() throws InterruptedException {
		// LOGGER.info("======= I am doing my Task @ {}", appConfig.getSpringAppName());
		AppContextUtil.clear();
		AppContextUtil.setTenant("app");
		AppContextUtil.init();
		LOGGER.debug("Searching Domains");

		String serviceDomain = pmEnvironment.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString();

		MongoQueryBuilder<ChannelConfigDupsDoc> emailChannelsQuery = MongoQueryBuilder
				.collection(ChannelConfigDupsDoc.class)
				.where(Criteria.where("contactType").is(ContactType.EMAIL.name()).and("server").is(serviceDomain));
		List<ChannelConfigDupsDoc> emailChannels = configMaster.find(emailChannelsQuery);

		for (ChannelConfigDupsDoc emailChannel : emailChannels) {
			LOGGER.debug("Searching Config {}", emailChannel.getId());
			if (!emailChannel.isDisabled() && !emailChannel.isDeleted()) {
				AppContextUtil.clear();
				AppContextUtil.setTenant(emailChannel.getDomain());
				AppContextUtil.init();
				if (ArgUtil.is(emailChannels) && emailChannels.size() > 0) {
					LOGGER.debug("Found Config {} ---> {}", emailChannel.getDomain(), emailChannel.getChannelId());
					inBoundPoller.throttle(new TunnelTask().name(InBoundPoller.TASK_EMAIL_POLLER)
							.id(emailChannel.getDomain() + "_" + emailChannel.getChannelId()).intervalSeconds(15)
							.data(MapModel.createInstance().put("channelId", emailChannel.getChannelId())));
				}
				AppContextUtil.clear();
			}
		}

	}

}
