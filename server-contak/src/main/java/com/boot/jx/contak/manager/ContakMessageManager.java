package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.contak.dto.PhoneNotpRequestModels.PhoneNotpDto;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.phonebook.doc.PhoneUserDoc;

public class ContakMessageManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public List<ContakMessageDoc> fetchMessages(PhoneUserDoc user) {
		TimeStampIndex deliveredAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(user.getPhoneId()).and("deliveredAt")
						.exists(false).and("expiredAt.hour").gte(deliveredAt.getHour() - 1))
				// Update
				.set("deliveredAt", deliveredAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("phoneId").is(user.getPhoneId()).and("deliveredAt.stamp")
						.is(deliveredAt.getStamp())));
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

	public List<ContakMessageDoc> setRead(String noteId) {
		TimeStampIndex readAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("noteId").is(noteId).and("readAt").exists(false))
				// Update
				.set("readAt", readAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(ContakMessageDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("noteId").is(noteId).and("readAt.stamp")
						.is(readAt.getStamp())));
	}

}
