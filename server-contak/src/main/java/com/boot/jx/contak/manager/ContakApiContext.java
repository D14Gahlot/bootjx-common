package com.boot.jx.contak.manager;

import java.util.concurrent.TimeUnit;

import org.bson.types.ObjectId;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.scope.ThreadScoped;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
@ThreadScoped
public class ContakApiContext {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	private CompanyDoc currentCompany;

	private static Cache<String, CompanyDoc> localConfigMap = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(5, TimeUnit.MINUTES).build();

	public CompanyDoc loadKey(String apiKey, boolean withSessionKey) {
		String[] keys = apiKey.split("\\-", 2);

		if (keys.length != 3) {
			return null;
		}
		String[] apiIndex = keys[0].split("\\:", 2);

		String clientId = apiIndex[0];
		String apiId = apiIndex[1];
		String apiSecret = keys[1];

		@Nullable
		CompanyDoc comp = localConfigMap.getIfPresent(apiId);

		if (comp == null) {
			comp = commonMongoTemplate.findOne(MQB.select(CompanyDoc.class)
					.where(QueryCriteria.where("api.$id").is(new ObjectId(apiId)).and("clientId").is(clientId)));
			if (comp != null) {
				localConfigMap.put(apiId, comp);
			} else {
				return null;
			}
		}

		if (comp == null || ArgUtil.not(comp.getApi())) {
			return null;
		}

		currentCompany = comp;

		if (withSessionKey && ArgUtil.is(apiSecret, comp.getApi().getSecretHash())) {
			return comp;
		}

		if (!CryptoUtil.getEncoder().message(apiSecret).sha2().is(comp.getApi().getSecretHash())) {
			return null;
		}

		return comp;
	}

	public CompanyDoc getCompany() {
		return currentCompany;
	}

}
