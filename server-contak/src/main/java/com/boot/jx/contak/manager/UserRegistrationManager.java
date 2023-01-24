package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;

@Component
public class UserRegistrationManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public List<UserRegistrationDoc> getContacts(String mobile) {
		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(UserRegistrationDoc.class)
				.where(Criteria.where("userPhoneNumber").is(mobile)));
	}

	public List<UserRegistrationDoc> fetchRegistrations(String companyId) {
		TimeStampIndex deliveredAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(UserRegistrationDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId).and("deliveredAt").exists(false)
						.and("expiredAt.hour").gte(deliveredAt.getHour() - 1))
				// Update
				.set("deliveredAt", deliveredAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(UserRegistrationDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId).and("deliveredAt.stamp")
						.is(deliveredAt.getStamp())));
	}

}
