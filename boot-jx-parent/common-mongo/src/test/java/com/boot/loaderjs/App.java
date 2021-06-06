package com.boot.loaderjs;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.github.gianlucanitti.javaexpreval.ExpressionException;

public class App { // Noncompliant

	private static Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws ExpressionException {

		String id = "608c981dda899d32d9636c67";

		Criteria c = Criteria.where("_id").is(new ObjectId(id)).orOperator(Criteria.where("created_by").is("ADMIN"));

		Query q = Query.query(c);

		System.out.println(q.toString());

		System.out.println(new CommonMongoQueryBuilder().whereId(id).getQuery().toString());

	}

}
