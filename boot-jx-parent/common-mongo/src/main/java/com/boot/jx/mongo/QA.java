package com.boot.jx.mongo;

import com.mongodb.BasicDBObject;

public class QA {
	public static BasicDBObject project(String field, BasicDBObject doc) {
		return new BasicDBObject("$project", new BasicDBObject(field, doc));
	}

	public static BasicDBObject objectToArray(String field) {
		return new BasicDBObject("$objectToArray", "$" + field);
	}
}