package com.boot.jx.mongo;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class CommonDocStore {

	@Autowired
	protected MongoConverter mongoConverter;

	@Autowired
	protected MongoTemplate mongoTemplate;

	public void applyPatch(Patchable<?> pacth) {
		DBObject update = getDbObject(pacth);
		mongoTemplate.updateFirst(query(where("_id").is(update.get("_id"))),
				Update.fromDBObject(new BasicDBObject("$set", update)), pacth.getClass());
	}

	public void applyPatch(Patchable<?> pacth, String collectionName) {
		DBObject update = getDbObject(pacth);
		mongoTemplate.updateFirst(query(where("_id").is(update.get("_id"))),
				Update.fromDBObject(new BasicDBObject("$set", update)), pacth.getClass(), collectionName);
	}

	private DBObject getDbObject(Object o) {
		BasicDBObject basicDBObject = new BasicDBObject();
		mongoConverter.write(o, basicDBObject);
		return basicDBObject;
	}
}
