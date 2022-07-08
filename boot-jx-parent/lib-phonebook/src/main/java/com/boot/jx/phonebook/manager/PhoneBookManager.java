package com.boot.jx.phonebook.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneContactDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;

@Component
public class PhoneBookManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(PhoneBookManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public List<PhoneContactDoc> getContacts(String mobile) {
		return commonMongoTemplate.find(
				CommonMongoQueryBuilder.collection(PhoneContactDoc.class).with(Criteria.where("userId").is(mobile)));
	}

	public List<PhoneContactDoc> getContacts(PhoneUserDoc user) {
		return getContacts(user.getMobile());
	}

}
