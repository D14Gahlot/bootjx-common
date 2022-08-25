package com.boot.jx.mongo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import com.mongodb.DBObject;

public class CommonMongoUtils {

	public static List<DBObject> newAggregation(AggregationOperation... aggOperations) {
		List<DBObject> agg = new ArrayList<DBObject>();
		for (AggregationOperation aggOperation : aggOperations) {
			agg.add(aggOperation.toDBObject(Aggregation.DEFAULT_CONTEXT));
		}
		return agg;
	}
}
