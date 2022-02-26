package com.boot.jx.mongo;

import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.jx.mongo.CommonDocInterfaces.MongoQueryBuilder;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampIndexSupport;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

public class CommonMongoQB<M extends CommonMongoQB<M, T>, T> implements MongoQueryBuilder<T> {

    public static class CommonMongoCriteria extends Criteria {
	public static Criteria whereId(Object id) {
	    return where("_id").is(id);
	}
    }

    Query query;
    Update update;
    Class<T> docClass;

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
    public M limit(long modifiedCount) {
	this.query().limit(ArgUtil.parseAsInteger(modifiedCount));
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

    public boolean isUpdatedTimeStampSupport() {
	return UpdatedTimeStampIndexSupport.class.isAssignableFrom(this.docClass);
    }

    public void updatedStamp() {
	long updatedStamp = System.currentTimeMillis();
	if (this.isUpdatedTimeStampSupport()) {
	    this.set("updated.stamp", updatedStamp);
	    this.set("updated.hour", updatedStamp / TimeUtils.Constants.MILLIS_IN_HOUR);
	    this.set("updated.day", updatedStamp / TimeUtils.Constants.MILLIS_IN_DAY);
	    this.set("updated.week", updatedStamp / TimeUtils.Constants.MILLIS_IN_WEEK);
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
