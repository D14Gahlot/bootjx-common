package com.boot.jx.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.utils.ArgUtil;

public class CommonMongoQueryBuilder {

	Query query;
	Update update;

	public Query query() {
		if (this.query == null) {
			query = new Query();
		}
		return query;
	}

	public Update update() {
		if (this.update == null) {
			update = new Update();
		}
		return update;
	}

	public CommonMongoQueryBuilder with(Criteria criteria) {
		query().addCriteria(criteria);
		return this;
	}

	public CommonMongoQueryBuilder where(String key, Object o) {
		query().addCriteria(Criteria.where(key).is(o));
		return this;
	}

	public CommonMongoQueryBuilder whereExists(String key) {
		query().addCriteria(Criteria.where(key).exists(true));
		return this;
	}

	public CommonMongoQueryBuilder whereId(Object id) {
		Criteria c = Criteria.where("_id").is(id);

		String idStr = ArgUtil.parseAsString(id);
		if (idStr != null && ObjectId.isValid(idStr)) {
			c.orOperator(Criteria.where("_id").is(new ObjectId(idStr)));
		}

		return this.with(c);
	}

	public CommonMongoQueryBuilder whereAll() {
		return this.whereExists("_id");
	}

	public CommonMongoQueryBuilder set(String key, Object o) {
		update().set(key, o);
		return this;
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}

	public Update getUpdate() {
		return update;
	}

	public void setUpdate(Update update) {
		this.update = update;
	}

}
