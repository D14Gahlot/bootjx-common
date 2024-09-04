package com.boot.jx.common.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.account.doc.waba.WabaPartnerDoc;
import com.boot.jx.mongo.CommonMongoTemplateAbstract.TenantDefaultMongoStore;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.utils.ArgUtil;

@Component
public class MasterDomainStore extends TenantDefaultMongoStore<MasterDomainStore> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MasterDomainStore.class);

	@Autowired
	private PMCommonConfig pmCommonConfig;

	public WabaPartnerDoc getPartnerWabaDoc(String partnerId, String cur) {
		String serviceServer = pmCommonConfig.getServiceServer();

		String serviceServerKey = serviceServer;

		if (ArgUtil.is(cur)) {
			serviceServerKey = serviceServerKey + "#" + cur;
		}

		WabaPartnerDoc partner = findByIdSafeCheck(serviceServerKey, WabaPartnerDoc.class);
		if (ArgUtil.not(partner) && ArgUtil.is(partnerId)) {
			partner = new WabaPartnerDoc();
			partner.setId(serviceServerKey);
			partner.setIsPrimaryPartner(true);
			partner.setPartnerId(partnerId);
		}
		return partner;
	}

	public WabaPartnerDoc getPartnerWabaDoc() {
		return this.getPartnerWabaDoc(null, null);
	}
}
