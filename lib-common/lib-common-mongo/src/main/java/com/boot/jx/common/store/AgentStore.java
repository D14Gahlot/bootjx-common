package com.boot.jx.common.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.mongo.CommonMongoQueryBuilder;

@Component
public class AgentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentStore.class);

	@Autowired
	MongoTemplate mongoTemplate;

	public List<AgentDoc> findAll() {
		return mongoTemplate.findAll(AgentDoc.class);
	}

	public AgentDoc findById(String agentId) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentId);
		return mongoTemplate.findOne(builder.getQuery(), AgentDoc.class);
	}

}
