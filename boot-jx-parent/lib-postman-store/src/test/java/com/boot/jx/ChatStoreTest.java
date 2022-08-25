package com.boot.jx;

import static org.junit.Assert.assertTrue;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.TestPropertySource;

import com.boot.jx.mongo.CommonMongoSource;
import com.boot.jx.mongo.CommonMongoUtils;
import com.boot.jx.mongo.MongoTemplateCommonImpl;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOptions.OutputMode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChatStoreTest { // Noncompliant

	@Value("${spring.data.mongodb.uri}")
	private String secondProperty;

	private MongoTemplateCommonImpl mongoTemplate;

	private void initMongo() {
		AppContextUtil.setTenant("demo");
		String connectionString = System.getProperty("spring.data.mongodb.uri");
		CommonMongoSource commonMongoSource = new CommonMongoSource();
		commonMongoSource.setDataSourceUrl(connectionString);
		commonMongoSource.setGlobalDataSourceUrl(connectionString);
		commonMongoSource.setGlobalDBProfix("tnt");

		mongoTemplate = new MongoTemplateCommonImpl(commonMongoSource.getMongoDbFactory());
		mongoTemplate.setMongoDBCredentials(commonMongoSource);
	}

	public static void main(String[] args) throws ParseException {
		String connectionString = System.getProperty("mongodb.uri");
		// System.out.println(connectionString);
//		try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//			MongoDatabase db = mongoClient.getDatabase("sample_training");
//			MongoCollection<Document> zips = db.getCollection("zips");
//			MongoCollection<Document> posts = db.getCollection("posts");
//			threeMostPopulatedCitiesInTexas(zips);
//			threeMostPopularTags(posts);
//		}
	}

	public static class LuckAgent {
		public Object agentCode;
		public Object quickskills;
		public Object noOfMatches;
		public long lastAssignStamp;
		public long lastOnlineStamp;

		LuckAgent from(DBObject doc) {
			this.lastAssignStamp = ArgUtil.parseAsLong(doc.get("lastAssignStamp"), Constants.DEFAULT_LONG);
			this.lastOnlineStamp = ArgUtil.parseAsLong(doc.get("lastOnlineStamp"), Constants.DEFAULT_LONG);
			this.agentCode = doc.get("_id");
			this.noOfMatches = doc.get("noOfMatches");
			return this;
		}
	}

	@Test
	public void testSkillMatch() {
		initMongo();
		System.out.println("=testSkillMatch==");
		List<String> list = new ArrayList<String>();
		list.add("dental");
		list.add("ortho");

		List<DBObject> agg = CommonMongoUtils.newAggregation(//
				match(Criteria.where("profile.quickskills.code").in(list)) //
				, project(bind("quickskills", "profile.quickskills.code").and("lastAssignStamp").and("lastOnlineStamp")
						.and("tags", "1"))//
				, unwind("quickskills")//
				, match(Criteria.where("quickskills").in(list)) //
				, group("_id").count().as("noOfMatches")//
						.first("lastAssignStamp").as("lastAssignStamp")//
						.first("lastOnlineStamp").as("lastOnlineStamp")//
				, sort(Direction.DESC, "noOfMatches").and(Direction.ASC, "lastOnlineStamp")
		//
		);
		System.out.println("=========================================");
		System.out.println("====" + JsonUtil.toJson(agg));
		System.out.println("=========================================");

		List<LuckAgent> other = new ArrayList<LuckAgent>();

//		AggregationResults<Result> groupResults = mongoTemplate.aggregate(agg, "AGENT_SESSION", Result.class);
//		groupResults.getMappedResults().forEach(doc -> other.add(doc));

		DBCollection col = mongoTemplate.getCollection("AGENT_SESSION");
		col.aggregate(agg, AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build())
				.forEachRemaining(doc -> other.add(new LuckAgent().from(doc)));
		System.out.println("=========================================");
		System.out.println("====" + JsonUtil.toJson(other));
		System.out.println("=========================================");
	}

	// @Test
	public void testLanguageEnumFromNumber() {
		initMongo();

		List<DBObject> list = new ArrayList<DBObject>();
//		list.add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))) // Match
//				.toDBObject(Aggregation.DEFAULT_CONTEXT));

		// Equivalent to $project
		DBObject projectFields = new BasicDBObject();
		projectFields.put("_id", 0);
		projectFields.put("category", "$_id");
		DBObject project = new BasicDBObject("$project", projectFields);

		List<QuickMedia> other = new ArrayList<QuickMedia>();
		list.add(Aggregation.group("category").count().as("count").toDBObject(Aggregation.DEFAULT_CONTEXT));
		list.add(project);

		try {
			DBCollection col = mongoTemplate.getCollection(mongoTemplate.getCollectionName(QuickMedia.class));

			col.aggregate(list, AggregationOptions.builder().allowDiskUse(true).outputMode(OutputMode.CURSOR).build())
					.forEachRemaining(doc -> other.add(new QuickMedia().from(doc)));

//			AggregationOutput output = col.aggregate(list);
//			// .forEachRemaining(doc -> other.add(new QuickMedia().from(doc)));
//
//			for (DBObject result : output.results()) {
//				System.out.println("====" + JsonUtil.toJson(result));
////	            BasicDBList employeeList = (BasicDBList) result.get("docs");
////	            BasicDBObject employeeDoc = (BasicDBObject) employeeList.get(0);
////	            String name = employeeDoc.get("name").toString();
////	            System.out.println(name);
//			}

			System.out.println("====" + JsonUtil.toJson(other));
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertTrue("Lang is not TL", true);
	}

}
