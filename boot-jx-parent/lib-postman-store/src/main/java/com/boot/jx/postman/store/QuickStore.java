package com.boot.jx.postman.store;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.mapreduce.GroupBy;
import org.springframework.data.mongodb.core.mapreduce.GroupByResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.doc.PMConfigurationDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.CompanyVarsConfigDoc;
import com.boot.jx.postman.doc.config.PrefsConfigDoc;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.mongodb.AggregationOptions;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.mongodb.AggregationOptions.OutputMode;

@Component
public class QuickStore extends CommonMongoTemplateAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(QuickStore.class);

	public List<QuickMedia> groupByCategory(Class<QuickMedia> clazz) {
		List<DBObject> list = new ArrayList<DBObject>();
//		list.add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))) // Match
//				.toDBObject(Aggregation.DEFAULT_CONTEXT));
		list.add(Aggregation.group("category").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));;
		DBCollection col = getCollection(getCollectionName(QuickStore.class));
		col.aggregate(list, AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.INLINE).build())
				.forEachRemaining(doc -> System.out.println(JsonUtil.toJson(doc)));

		GroupByResults<QuickMedia> g = group(getCollectionName(QuickStore.class), GroupBy.key("category"),
				QuickMedia.class);

		g.iterator().forEachRemaining(doc -> System.out.println(JsonUtil.toJson(doc)));

		return null;
	}

}
