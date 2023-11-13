package com.boot.jx.admin.manager;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.tpo.WABAConversation;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.JsonUtil;

@Component
public class SessionExpirySchedular  extends CommonMongoTemplateAbstract<SessionStore> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SessionExpirySchedular.class);
	
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	PMEnvironment pmEnvironment;
	

	/** seconds minutes hours day-of-month month day-of-week
   			0       0      8        *         *        ?
   			
   			for every 1 hour : * * 0/60 * * *
   			
   **/
	
	@SuppressWarnings("deprecation")
	//@Scheduled(cron = "0 * 1 * * *")
	@Scheduled(fixedRate=60*60*1000)
	public void sessionExpirySchedular() {
		LOGGER.info("Current time is :: " + LocalDate.now());
		
		SafeKeyHashMap<Object> globalVars = pmEnvironment.local().globalVars();
		boolean isSchedular = false;//globalVars.keyEntry("session_expiry_schedular").asBoolean();
		LOGGER.info("isSchedular ON/OFF :"+isSchedular);
		if(isSchedular) {
		
		
		Query query = new Query(); 
		query.addCriteria(Criteria.where("contactType").is(ContactType.WHATSAPP.name()).and("primary").is(true));
	
		//excludeMsgFields(query);
		List<ChatSessionDoc> chatSessions = mongoTemplate.find(query, ChatSessionDoc.class,AgentAnalyticsManager.CHAT_SESSION);
		
		for(ChatSessionDoc chatSessionDoc :chatSessions) {
			String csid =chatSessionDoc.getContact().getCsid();
			long startSessionStamp = chatSessionDoc.getStartSessionStamp();
			System.out.println("csid :"+csid+"\t startSessionStamp :"+startSessionStamp+"\t Id :"+chatSessionDoc.getSessionId());
			
			Query wabaQry = new Query();
			wabaQry.addCriteria(Criteria.where("contact.csid").is(csid));
			wabaQry.with(new Sort(new Order(Direction.DESC, "created.stamp")));
			List<WABAConversation> wabaDoc =  mongoTemplate.find(wabaQry,WABAConversation.class,"TP_WABA_CONVERSATIONS");
			if(wabaDoc!=null && !wabaDoc.isEmpty()) {
				WABAConversation wabaConversation = wabaDoc.get(0);//Long.valueOf(jo.get("ipInt").toString());
				long expiryTimeStamp =Long.valueOf(wabaConversation.getConversation().get("expiration_timestamp").toString());
				
				/** update chat session with expiry timestamp **/
				chatSessionDoc.setSessionExpiryStamp(expiryTimeStamp);
				MongoQueryBuilder<ChatSessionDoc> builder = MongoQueryBuilder.collection(ChatSessionDoc.class)
						.whereId(chatSessionDoc.getSessionId());
				builder.set("sessionExpiryStamp", expiryTimeStamp);
				super.updateFirst(builder.getQuery(), builder.getUpdate(), ChatSessionDoc.class);
				/** end ----------**/
		}
		}
	}else {
		LOGGER.info("Schedular is not active ");
	}
	}
	public void excludeMsgFields(Query query) {
		query.fields().exclude("updated")
		.exclude("msg.lastInBoundMsg")		
		.exclude("stamps");
	}
}
