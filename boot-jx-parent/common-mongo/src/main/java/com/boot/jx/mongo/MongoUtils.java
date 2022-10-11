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
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

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
		protected AggregateIterable<T> aggregate;

		public MongoResultProcessor<T> using(MongoTemplate mongoTemplate) {
			this.mongoTemplate = mongoTemplate;
			return this;
		}

		public MongoResultProcessor<T> collection(String collection) {
			this.col = mongoTemplate.getCollection(collection);
			return this;
		}

		public MongoResultProcessor<T> aggregate(List<Document> aggreQuery, Class<T> resultClass) {
			aggregate = col.aggregate(aggreQuery, resultClass);
			return this;
		}

		public MongoResultProcessor<T> aggregate(AggregateIterable<T> aggregate) {
			this.aggregate = aggregate;
			return this;
		}

		public MongoResultProcessor<Document> aggregate(List<Document> aggreQuery) {
			SimpleMongoResultProcessor newP = new SimpleMongoResultProcessor();
			return newP.aggregate(col.aggregate(aggreQuery));
		}

		public MongoResultProcessor<T> forEach(Consumer<? super T> action) {
			this.aggregate.forEach(action);
			return this;
		}

		public List<T> asList(List<T> list) {
			MongoCursor<T> cursor = this.aggregate.iterator();
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

		public <TField> List<TField> distinctValues(String fieldKey, Class<TField> fieldClazz) {
			return CollectionUtil.asList(col.distinct(fieldKey, fieldClazz));
		}
	}

	public static class SimpleMongoResultProcessor extends MongoResultProcessor<Document> {

	}

}
