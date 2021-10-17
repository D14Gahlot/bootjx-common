package com.boot.jx.mongo;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCursor;

public class MongoUtils {

    public static <T> List<T> toList(DistinctIterable<T> distinctIterable) {
	List<T> categoryList = new ArrayList<T>();
	MongoCursor<T> cursor = distinctIterable.iterator();
	while (cursor.hasNext()) {
	    T category = cursor.next();
	    categoryList.add(category);
	}
	return categoryList;
    }
}
