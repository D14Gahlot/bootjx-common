
package com.boot.jx.mongo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;

public class MongoUtils {

	public static <T> List<T> toList(DistinctIterable<T> distinctIterable) {
		return CollectionUtil.asList(distinctIterable);
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

	public static class MongoResultProcessor<T> {
		protected MongoTemplate mongoTemplate;
		protected MongoCollection<Document> col;
		protected MongoIterable<T> results;

		public MongoResultProcessor<T> using(MongoTemplate mongoTemplate) {
			this.mongoTemplate = mongoTemplate;
			return this;
		}

		public MongoResultProcessor<T> collection(String collection) {
			this.col = mongoTemplate.getCollection(collection);
			return this;
		}

		public MongoResultProcessor<T> results(MongoIterable<T> aggregate) {
			this.results = aggregate;
			return this;
		}

		public <TResult> MongoResultProcessor<TResult> aggregate(List<Document> aggreQuery,
				Class<TResult> resultClass) {
			MongoResultProcessor<TResult> newP = new MongoResultProcessor<TResult>();
			return newP.results(col.aggregate(aggreQuery, resultClass));
		}

		public MongoResultProcessor<Document> aggregate(List<Document> aggreQuery) {
			SimpleMongoResultProcessor newP = new SimpleMongoResultProcessor();
			return newP.results(col.aggregate(aggreQuery));
		}

		public <TResult> MongoResultProcessor<TResult> aggregate(QA aggreQuery, Class<TResult> resultClass) {
			return this.aggregate(aggreQuery.piplines(), resultClass);
		}

		public MongoResultProcessor<Document> aggregate(QA aggreQuery) {
			return this.aggregate(aggreQuery.piplines());
		}

		public MongoResultProcessor<T> distinct(String fieldkey, Class<T> fieldkeyType) {
			results = col.distinct(fieldkey, fieldkeyType);
			return this;
		}

		public MongoResultProcessor<String> distinct(String fieldkey) {
			MongoResultProcessor<String> newP = new MongoResultProcessor<String>();
			return newP.results(col.distinct(fieldkey, String.class));
		}

		public MongoResultProcessor<T> forEach(Consumer<? super T> action) {
			this.results.forEach(action);
			return this;
		}

		public MongoCursor<T> iterator() {
			return this.results.iterator();
		}

		public List<T> asList(List<T> list) {
			MongoCursor<T> cursor = this.results.iterator();
			while (cursor.hasNext()) {
				T object = cursor.next();
				if (ArgUtil.is(object)) {
					list.add(object);
				}

			}
			return list;
		}

		public List<T> asList() {
			return asList(new LinkedList<T>());
		}

	}

	public static class SimpleMongoResultProcessor extends MongoResultProcessor<Document> {

	}

}
