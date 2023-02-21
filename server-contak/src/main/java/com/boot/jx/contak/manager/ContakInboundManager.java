package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.dto.ContakInboundDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;

@Component
public class ContakInboundManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContakInboundManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public List<ContakInboundDoc> fetchInbounds(String companyId) {
		TimeStampIndex notifiedAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakInboundDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId)
						// only if it has not been notified yet
						.and("notifiedAt").exists(false)
						// Consider expiry of inbound only if it was defined at the time of creation
						.orOperator(Criteria.where("expiredAt.hour").gte(notifiedAt.getHour() - 1),
								Criteria.where("expiredAt").exists(false)))
				// Update
				.set("notifiedAt", notifiedAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(ContakInboundDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId).and("notifiedAt.stamp")
						.is(notifiedAt.getStamp())));
	}

}
