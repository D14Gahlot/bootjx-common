package com.boot.jx.mongo;

import java.util.List;

import org.bson.Document;

import com.boot.utils.CollectionUtil;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;

public class MongoUtils {

	public static <T> List<T> toList(DistinctIterable<T> distinctIterable) {
		return CollectionUtil.asList(distinctIterable);
	}

	public static <T> List<T> distinct(MongoCollection<Document> collection, String key, Class<T> clazz) {
		return CollectionUtil.asList(collection.distinct(key, clazz));
	}
}
