package com.boot.jx.account.api;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;

@Component
public class AccountStore extends CommonMongoTemplateAbstract {

    public AccountDoc findOneByEmail(String email, Class<AccountDoc> clazz) {
	Query query2 = new Query();
	query2.addCriteria(Criteria.where("contact.email").is(email));
	AccountDoc account = findOne(query2, AccountDoc.class);
	return account;
    }

}
