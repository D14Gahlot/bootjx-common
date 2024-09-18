package com.boot.jx;

import static org.junit.Assert.assertTrue;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.bind;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.test.context.TestPropertySource;

import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.MongoUtils;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ContactDetailDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.store.MessageStore;
import com.boot.test.BaseMongoTestSetup;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class ChatStoreTest extends BaseMongoTestSetup { // Noncompliant

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	@Value("${spring.data.mongodb.uri}")
	private String secondProperty;

	public PBPhone parsePhone(PBPhone pbPhone) {
		String defaultRegion = "IN";
		if (ArgUtil.not(pbPhone.phone)) {
			pbPhone.phone = String.format("+%s%s", pbPhone.countryCallingCode, pbPhone.nationalNumber);
		}
		pbPhone.phone = pbPhone.phone.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();
		try {
			PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse("+" + pbPhone.phone, defaultRegion);
			pbPhone.nationalNumber = ArgUtil.parseAsString(phoneNumber.getNationalNumber());
			pbPhone.countryCallingCode = ArgUtil.parseAsString(phoneNumber.getCountryCode());
			pbPhone.phone = String.format("+%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			pbPhone.country = PHONE_NUMBER_UTIL.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
		} catch (NumberParseException e) {

		}

		return pbPhone;
	}

	public static void main(String[] args) throws ParseException {
		String connectionString = System.getProperty("mongodb.uri");
		System.out.println(connectionString);
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

		LuckAgent from(Document doc) {
			this.lastAssignStamp = ArgUtil.parseAsLong(doc.get("lastAssignStamp"), Constants.DEFAULT_LONG);
			this.lastOnlineStamp = ArgUtil.parseAsLong(doc.get("lastOnlineStamp"), Constants.DEFAULT_LONG);
			this.agentCode = doc.get("_id");
			this.noOfMatches = doc.get("noOfMatches");
			return this;
		}
	}

	@Test
	public void chatSessionTest() {
		ensureMongoConnection();
		ChatSessionDoc chatSessionDoc = new ChatSessionDoc();
		chatSessionDoc.setContact(new ContactDetailDoc());

		mongoTemplate.save(chatSessionDoc);
		ChatSessionDoc session = mongoTemplate.findById(chatSessionDoc.getSessionId(), ChatSessionDoc.class);
		System.out.println("=========================================");
		if (ArgUtil.is(session)) {
			System.out.println("====" + JsonUtil.toJson(session));
		} else {
			System.out.println("==== Not found for " + chatSessionDoc.getSessionId());
		}
		System.out.println("=========================================");
	}

	@Test
	public void contactableTest() {
		ensureMongoConnection();

		MessageDoc msg = new MessageDoc();
		mongoTemplate.save(msg, MessageStore.getCollectionName(ContactType.DUMMY));

		msg = mongoTemplate.findById(msg.getMessageId(), MessageDoc.class,
				MessageStore.getCollectionName(ContactType.DUMMY));
		System.out.println("=========================================");
		if (ArgUtil.is(msg)) {
			System.out.println("====" + JsonUtil.toJson(msg));
		} else {
			System.out.println("==== Not found for " + msg.getSessionId());
		}
		System.out.println("=========================================");
	}

	// @Test
	public void profile() {
		ensureMongoConnection();
		PBPhone ph = parsePhone(new PBPhone().phone("91993104050"));
		MongoQueryBuilder<CustomerProfileDoc> qb = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.where(Criteria.where("phones").elemMatch(Criteria.where("nationalNumber").is(ph.nationalNumber)
						.and("countryCallingCode").is(ph.countryCallingCode)));

		System.out.println("====" + qb.getQuery().toString());

		List<CustomerProfileDoc> profiles = mongoTemplate.find(qb.getQuery(), CustomerProfileDoc.class);

		System.out.println("=========================================");
		for (CustomerProfileDoc customerProfileDoc : profiles) {
			System.out.println("====" + JsonUtil.toJson(customerProfileDoc));
		}
		System.out.println("=========================================");
	}

	// @Test
	public void testSkillMatch() {
		ensureMongoConnection();
		System.out.println("=testSkillMatch==");
		List<String> list = new ArrayList<String>();
		list.add("dental");
		list.add("ortho");

		List<Document> agg = MongoUtils.newAggregation(//
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
		try {
			System.out.println("=========================================");
			System.out.println("====>" + JsonUtil.toJson(agg));
			System.out.println("=========================================");

			List<LuckAgent> other = new ArrayList<LuckAgent>();

			mongoTemplate.collection("AGENT_SESSION").aggregate(agg)
					.forEach(doc -> other.add(new LuckAgent().from(doc)));

			System.out.println("=========================================");
			System.out.println("====|" + JsonUtil.toJson(other));
			System.out.println("=========================================");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testLanguageEnumFromNumber() throws Exception {
		ensureMongoConnection();

		List<Document> list = new ArrayList<Document>();
//		list.add(Aggregation.match(Criteria.where("bulkSessionId").is((currentBatchJob.getJobId()))) // Match
//				.toDBObject(Aggregation.DEFAULT_CONTEXT));

		// Equivalent to $project
		DBObject projectFields = new BasicDBObject();
		projectFields.put("_id", 0);
		projectFields.put("category", "$_id");
		Document project = new Document("$project", projectFields);

		List<QuickMedia> other = new ArrayList<QuickMedia>();
		list.add(Aggregation.group("category").count().as("count").toDocument(Aggregation.DEFAULT_CONTEXT));
		list.add(project);

		try {
			MongoCollection<Document> col = mongoTemplate
					.getCollection(mongoTemplate.getCollectionName(QuickMedia.class));

			MongoCursor<Document> cursor = col.aggregate(list).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				other.add(new QuickMedia().from(doc));
			}

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
