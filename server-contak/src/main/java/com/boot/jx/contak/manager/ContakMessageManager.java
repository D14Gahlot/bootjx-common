package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.postman.doc.MessageDoc.MessageDocLogs;
import com.boot.utils.ArgUtil;

@Component
public class ContakMessageManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	ContakInboundManager contakInboundManager;

	public List<ContakMessageDoc> fetchMessages(PhoneUserDoc user) {
		TimeStampIndex deliveredAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(user.getPhoneId()).and("deliveredAt")
						.exists(false).and("expiredAt.hour").gte(deliveredAt.getHour() - 1))
				// Update
				.set("deliveredAt", deliveredAt));

		List<ContakMessageDoc> messages = commonMongoTemplate
				.find(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
						CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(user.getPhoneId())
								.and("deliveredAt.stamp").is(deliveredAt.getStamp())));

		for (ContakMessageDoc contakMessageDoc : messages) {
			contakInboundManager.sendMsgDelvryEvent(contakMessageDoc);
		}
		return messages;
	}

	public List<ContakMessageDoc> setDelivery(String noteId) {
		TimeStampIndex deliveredAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("noteId").is(noteId).and("deliveredAt").exists(false))
				// Update
				.set("deliveredAt", deliveredAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("noteId").is(noteId).and("deliveredAt.stamp")
						.is(deliveredAt.getStamp())));
	}

	public List<ContakMessageDoc> markRead(String noteId) {
		ContakMessageDoc readMessage = commonMongoTemplate
				.findOne(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).whereId("noteId"));

		if (!ArgUtil.is(readMessage) || !ArgUtil.is(readMessage.getDeliveredAt())) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("noteId"));
		}

		TimeStampIndex readAt = TimeStampIndex.from(System.currentTimeMillis());

		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(readMessage.getPhoneId()) // Phone Id
						.and("domain").is(readMessage.getDomain()) // Domain
						.and("companyId").is(readMessage.getCompanyId()) // Company
						.and("deliveredAt").exists(true) // delievery exists
						.and("deliveredAt.stamp").lte(readMessage.getDeliveredAt().getStamp())// Delivery
		)
				// Update
				.set("readAt", readAt));

		List<ContakMessageDoc> messages = commonMongoTemplate
				.find(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
						CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(readMessage.getPhoneId())
								.and("domain").is(readMessage.getDomain()) // Domain
								.and("companyId").is(readMessage.getCompanyId()) // Company
								.and("readAt.stamp").is(readAt.getStamp())));

		contakInboundManager.sendMsgReadEventAsync(messages);

		return messages;
	}

}
