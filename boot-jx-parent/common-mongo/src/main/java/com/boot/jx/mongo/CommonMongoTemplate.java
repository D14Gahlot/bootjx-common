package com.boot.jx.mongo;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonDocInterfaces.TrashDocument;
import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;
import com.boot.utils.ArgUtil;
import com.mongodb.WriteResult;

@Component
public class CommonMongoTemplate extends CommonMongoTemplateDefault {

	@Autowired
	protected MongoTemplate mongoTemplate;

	protected MongoConverter mongoConverter;

	@Autowired(required = false)
	private AuditDetailProvider auditDetailProvider;

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

	/**
	 * @param builder
	 * @return
	 * 
	 * @see MongoTemplate#upsert(Query,
	 *      org.springframework.data.mongodb.core.query.Update, Class, String)
	 */
	public WriteResult upsert(DocQueryBuilder<?> builder) {
		return mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), builder.getDocClass());
	}

	public WriteResult trash(Object object) {
		if (object instanceof AuditableEntity && ArgUtil.is(auditDetailProvider)) {
			String collectionName = "TRASH_" + mongoTemplate.getCollectionName(object.getClass());
			auditDetailProvider.audit((AuditableEntity) object);
			mongoTemplate.save(new TrashDocument().doc(object), collectionName);
		}
		return getCommonMongoTemplate().remove(object);
	}

}
