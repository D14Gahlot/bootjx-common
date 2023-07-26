package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class ContakMessageManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private ContakInboundRouter otpAlertEventManager;

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

		if (ArgUtil.is(messages) && messages.size() > 0) {
			otpAlertEventManager.sendMsgDelvryEventAsync(messages);
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
				.findOne(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).whereId(noteId));

		if (!ArgUtil.is(readMessage) || !ArgUtil.is(readMessage.getDeliveredAt())) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("noteId"));
		}

		if (ArgUtil.is(readMessage.getReadAt())) {
			return CollectionUtil.asList(readMessage);
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

		otpAlertEventManager.sendMsgReadEventAsync(messages);

		return messages;
	}

	public List<ContakMessageDoc> addEventLog(MessageEvent event) {
		ContakMessageDoc failedMessage = commonMongoTemplate
				.findOne(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).whereId(event.noteId));

		if (!ArgUtil.is(failedMessage) || !ArgUtil.is(failedMessage.getDeliveredAt())) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("noteId"));
		}

		if (ArgUtil.is(failedMessage.getReadAt())) {
			return CollectionUtil.asList(failedMessage);
		}

		event.eventStamp = System.currentTimeMillis();
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.whereId(event.noteId) // NoteId
						.and("phoneId").is(failedMessage.getPhoneId()) // Phone Id
						.and("domain").is(failedMessage.getDomain()) // Domain
						.and("companyId").is(failedMessage.getCompanyId()) // Company
		)
				// Update
				.push("events", event));

		otpAlertEventManager.sendMsgLogEventAsync(failedMessage, event);

		return CollectionUtil.asList(failedMessage);
	}

}
