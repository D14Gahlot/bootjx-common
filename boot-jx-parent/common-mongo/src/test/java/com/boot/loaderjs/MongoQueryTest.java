package com.boot.loaderjs;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;

import com.boot.jx.mongo.CommonMongoQB.CommonMongoQBimpl;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.utils.FileUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonUtil;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

public class MongoQueryTest { // Noncompliant

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ParseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParseException, IOException {
		CommonMongoQBimpl<MongoQueryTest> qa = CommonMongoQueryBuilder.collection(MongoQueryTest.class)
				.where(Criteria.where("category").regex("^test$", "i"));
		System.out.println(qa.query().toString());

	}

}
