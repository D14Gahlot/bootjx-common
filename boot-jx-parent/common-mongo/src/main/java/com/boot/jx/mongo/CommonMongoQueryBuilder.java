package com.boot.jx.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.utils.ArgUtil;

public class CommonMongoQueryBuilder {

	public static class CommonMongoCriteria extends Criteria {
		public static Criteria whereId(Object id) {
			return where("_id").is(id);
		}
	}

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

	/**
	 * This is fail Safe '_id' based Search, if Document has 'id' as field
	 * 
	 * @param id
	 * @return
	 */
	public CommonMongoQueryBuilder whereIdSafe(Object id) {
		Criteria c = Criteria.where("_id").is(id);
		String idStr = ArgUtil.parseAsString(id);
		if (idStr != null && ObjectId.isValid(idStr)) {
			Criteria altC = Criteria.where("_id").is(new ObjectId(idStr));
			c = new Criteria().orOperator(c, altC);
		}
		return this.with(c);
	}

	public CommonMongoQueryBuilder whereId(Object id) {
		return this.with(CommonMongoCriteria.whereId(id));
	}

	public CommonMongoQueryBuilder whereAll() {
		query().addCriteria(new Criteria());
		return this;
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

	public static abstract class DocQueryBuilder<T> extends CommonMongoQueryBuilder {
		protected T doc;

		public DocQueryBuilder(T doc) {
			this.doc = doc;
			whereId(getId(this.doc));
		}

		public DocQueryBuilder(String id) {
			this.doc = this.newDoc(id);
			whereId(id);
		}

		public Class<?> getDocClass() {
			return this.doc.getClass();
		}

		public abstract T newDoc(String id);

		public abstract String getId(T doc);
	}

}
