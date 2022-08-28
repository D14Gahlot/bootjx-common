package com.boot.jx.mongo;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import com.boot.utils.CollectionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;

public class MongoUtils {

	public static <T> List<T> toList(DistinctIterable<T> distinctIterable) {
		return CollectionUtil.asList(distinctIterable);
	}
	
	public static <T> List<T> distinct(MongoCollection<Document> collection, String key, Class<T> clazz) {
		return CollectionUtil.asList(collection.distinct(key, clazz));
	}

	public static List<Document> newAggregation(AggregationOperation... aggOperations) {
		List<Document> agg = new ArrayList<Document>();
		for (AggregationOperation aggOperation : aggOperations) {
			agg.add(aggOperation.toDocument(Aggregation.DEFAULT_CONTEXT));
		}
		return agg;
	}

	public static List<DBObject> newAggregationDBObject(AggregationOperation... aggOperations) {
		List<DBObject> agg = new ArrayList<DBObject>();
		for (AggregationOperation aggOperation : aggOperations) {
			agg.add(new BasicDBObject(aggOperation.toDocument(Aggregation.DEFAULT_CONTEXT)));;
		}
		return agg;
	}
}
