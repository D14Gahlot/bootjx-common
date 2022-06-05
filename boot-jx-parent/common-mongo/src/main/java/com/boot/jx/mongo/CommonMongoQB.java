package com.boot.jx.mongo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.jx.mongo.CommonDocInterfaces.MongoQueryBuilder;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.CreatedTimeStampIndexSupport;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampIndexSupport;
import com.boot.utils.ArgUtil;

public class CommonMongoQB<M extends CommonMongoQB<M, T>, T> implements MongoQueryBuilder<T> {

	public static class CommonMongoCriteria extends Criteria {
		public static Criteria whereId(Object id) {
			return where("_id").is(id);
		}

		public static Criteria whereCode(Object code) {
			return where("code").is(code);
		}
	}

	Query query;
	Update update;
	Class<T> docClass;
	private boolean skipUpdateStamp;

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

	@SuppressWarnings("unchecked")
	public M query(Query query) {
		this.query = query;
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M with(Criteria criteria) {
		query().addCriteria(criteria);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M where(String key, Object o) {
		query().addCriteria(Criteria.where(key).is(o));
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M having(String key) {
		query().addCriteria(Criteria.where(key).exists(true));
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M sortBy(String byField) {
		this.query().with(new Sort(Direction.ASC, byField));
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M sortBy(String byField, Direction inDir) {
		this.query().with(new Sort(inDir, byField));
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M limit(int limit) {
		this.query().limit(limit);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M skip(int skip) {
		this.query().skip(skip);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M page(int pageNo, int pageSize) {
		int pageStart = pageNo * pageSize;
		int pageEnd = pageStart + pageSize;
		this.query().limit(pageSize).skip(pageStart);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M skipStampUpdate() {
		this.skipUpdateStamp = true;
		return (M) this;
	}

	/**
	 * This is fail Safe '_id' based Search, if Document has 'id' as field
	 * 
	 * @param id
	 * @return
	 */
	public M whereIdSafe(Object id) {
		Criteria c = Criteria.where("_id").is(id);
		String idStr = ArgUtil.parseAsString(id);
		if (idStr != null && ObjectId.isValid(idStr)) {
			Criteria altC = Criteria.where("_id").is(new ObjectId(idStr));
			c = new Criteria().orOperator(c, altC);
		}
		return this.with(c);
	}

	public M whereId(Object id) {
		return this.with(CommonMongoCriteria.whereId(id));
	}

	public M whereCode(Object code) {
		return this.with(CommonMongoCriteria.whereCode(code));
	}

	@SuppressWarnings("unchecked")
	public M whereAll() {
		query().addCriteria(new Criteria());
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M set(String key, Object o) {
		update().set(key, o);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M setOnInsert(String key, Object o) {
		update().setOnInsert(key, o);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M unset(String key) {
		update().unset(key);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M ref(String key, String id, String collectionName) {
		Map<String, Object> ref = new HashMap<String, Object>();
		ref.put("$ref", collectionName);
		ref.put("$id", new ObjectId(id));
		update().set(key, ref);
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M skipDBRef() {
		if (ArgUtil.is(this.getDocClass())) {
			org.springframework.data.mongodb.core.query.Field fields = this.query().fields();
			for (Field field : this.getDocClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(DBRef.class)) {
					fields.exclude(field.getName());
				}
			}
		}
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M skipDBRefByNames(String... fieldNames) {
		org.springframework.data.mongodb.core.query.Field fields = this.query().fields();
		for (String field : fieldNames) {
			fields.exclude(field);
		}
		return (M) this;
	}

	@SuppressWarnings("unchecked")
	public M includeDBRef(String field) {
		this.query().fields().include(field);
		return (M) this;
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

	public Class<T> getDocClass() {
		return docClass;
	}

	public void setDocClass(Class<T> docClass) {
		this.docClass = docClass;
	}

	@Override
	public boolean isUpdatedTimeStampSupport() {
		if (ArgUtil.is(this.docClass)) {
			return UpdatedTimeStampIndexSupport.class.isAssignableFrom(this.docClass);
		}
		return false;
	}

	@Override
	public boolean isCreatedTimeStampSupport() {
		if (ArgUtil.is(this.docClass)) {
			return CreatedTimeStampIndexSupport.class.isAssignableFrom(this.docClass);
		}
		return false;
	}

	public void updatedStamp() {
		long updatedStamp = System.currentTimeMillis();
		boolean isUpdatedTimeStampSupport = this.isUpdatedTimeStampSupport();
		boolean isCreatedTimeStampSupport = this.isCreatedTimeStampSupport();

		if (isUpdatedTimeStampSupport || isCreatedTimeStampSupport) {
			TimeStampIndex timeStampIndex = TimeStampIndex.from(updatedStamp);
			if (isUpdatedTimeStampSupport && !skipUpdateStamp) {
				this.set("updated.stamp", timeStampIndex.getStamp());
				this.set("updated.hour", timeStampIndex.getHour());
				this.set("updated.day", timeStampIndex.getDay());
				this.set("updated.week", timeStampIndex.getWeek());
			}
			if (isCreatedTimeStampSupport) {
				this.setOnInsert("created", timeStampIndex);
			}
		}
		this.set("updatedStamp", updatedStamp);
	}

	public static class CommonMongoQBimpl<R> extends CommonMongoQB<CommonMongoQBimpl<R>, R> {

	}

	public static <T> CommonMongoQB<CommonMongoQBimpl<T>, T> collection(Class<T> docClass) {
		CommonMongoQBimpl<T> x = new CommonMongoQBimpl<T>();
		x.setDocClass(docClass);
		return x;
	}

}
