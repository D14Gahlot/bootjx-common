package com.boot.jx.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.utils.ArgUtil;

@Component
public class UtilityMongoTemplate {

	@Autowired
	protected MongoTemplate mongoTemplate;

	public <T> T findById(String id, Class<T> clazz) {
		if (ArgUtil.is(id)) {
			return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), clazz);
		}
		return null;
	}

	public <T extends DocVersion> T creatNewDocuemnt(String id, Class<T> clazz, T newVersion) {
		if (ArgUtil.is(id)) {
			T oldVersion = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), clazz);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
			}
		}
		return newVersion;
	}

}
