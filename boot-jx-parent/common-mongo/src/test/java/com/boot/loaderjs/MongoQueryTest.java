package com.boot.loaderjs;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.data.mongodb.core.query.Criteria;

import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;

public class MongoQueryTest { // Noncompliant

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParseException, IOException {
		MongoQueryBuilder<MongoQueryTest> qa = CommonMongoQueryBuilder.collection(MongoQueryTest.class)
				.where(Criteria.where("category").regex("^test$", "i"));
		System.out.println(qa.query().toString());

	}

}
