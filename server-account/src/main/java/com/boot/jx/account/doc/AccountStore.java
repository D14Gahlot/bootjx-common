package com.boot.jx.account.doc;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;

@Component
public class AccountStore extends CommonMongoTemplateAbstract {

    public <T> List<T> findByKey(String key, String value, Class<T> clazz) {
	Query query2 = new Query();
	query2.addCriteria(Criteria.where(key).is(value));
	List<T> docs = find(query2, clazz);
	return docs;
    }

    public <T> T findOneByKey(String key, String value, Class<T> clazz) {
	Query query2 = new Query();
	query2.addCriteria(Criteria.where(key).is(value));
	T doc = findOne(query2, clazz);
	return doc;
    }

    public DomainUserDoc findOneByEmail(String email, Class<DomainUserDoc> clazz) {
	return findOneByKey("contact.email", email, DomainUserDoc.class);
    }

    public DomainDoc findDomainByName(String domain) {
	return findOneByKey("domain", domain, DomainDoc.class);
    }
}
