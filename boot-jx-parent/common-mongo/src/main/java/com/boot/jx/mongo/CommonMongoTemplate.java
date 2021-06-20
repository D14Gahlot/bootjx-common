package com.boot.jx.mongo;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.utils.ArgUtil;
import com.mongodb.WriteResult;

@Component
public class CommonMongoTemplate extends CommonMongoTemplateDefault {

	@Autowired
	protected MongoTemplate mongoTemplate;

	protected MongoTemplate getCommonMongoTemplate() {
		return mongoTemplate;
	}

	public <T> T findByIdString(String id, Class<T> clazz) {
		if (ArgUtil.is(id)) {
			Criteria c = Criteria.where("_id").is(id);
			return getCommonMongoTemplate().findOne(new Query(c), clazz);
		}
		return null;
	}

	public <T> T findByIdSafeCheck(Object id, Class<T> clazz) {
		Criteria c = Criteria.where("_id").is(id);
		String idStr = ArgUtil.parseAsString(id);
		if (idStr != null && ObjectId.isValid(idStr)) {
			Criteria altC = Criteria.where("_id").is(new ObjectId(idStr));
			c = new Criteria().orOperator(c, altC);
		}
		if (ArgUtil.is(id)) {
			return getCommonMongoTemplate().findOne(new Query(c), clazz);
		}
		return null;
	}

	public <T extends DocVersion> T creatNewDocuemnt(String id, Class<T> clazz, T newVersion) {
		if (ArgUtil.is(id)) {
			T oldVersion = getCommonMongoTemplate().findOne(new Query(Criteria.where("_id").is(id)), clazz);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
			}
		}
		return newVersion;
	}

	public WriteResult updateFirst(DocQueryBuilder<?> builder) {
		return mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), builder.getDocClass());
	}

	public WriteResult upsert(DocQueryBuilder<?> builder) {
		return mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), builder.getDocClass());
	}

}
