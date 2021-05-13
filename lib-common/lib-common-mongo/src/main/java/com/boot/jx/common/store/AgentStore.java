package com.boot.jx.common.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.utils.ArgUtil;

@Component
public class AgentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentStore.class);

	@Autowired
	MongoTemplate mongoTemplate;

	public void updateMulti(CommonMongoQueryBuilder builder, Class<?> entityClass) {
		mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), entityClass);
	}

	public List<AgentDoc> findAll() {
		return mongoTemplate.findAll(AgentDoc.class);
	}

	public AgentDoc findById(String agentId) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentId);
		return mongoTemplate.findOne(builder.getQuery(), AgentDoc.class);
	}

	public List<DepartmentDoc> findDepartmentAll() {
		return mongoTemplate.findAll(DepartmentDoc.class);
	}

	public DepartmentDoc findDepartmentById(String deptId) {
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(deptId);
		return mongoTemplate.findOne(builder.getQuery(), DepartmentDoc.class);
	}

	public String findDepartmentCodeById(String deptId) {
		if (ArgUtil.is(deptId)) {
			DepartmentDoc dept = findDepartmentById(deptId);
			if (ArgUtil.is(dept)) {
				return dept.getDept_code();
			}
		}
		return null;
	}

	public void updateAgentDefault(String agent_id) {
		AgentDoc agent = findById(agent_id);

		CommonMongoQueryBuilder cqb = new CommonMongoQueryBuilder().where("dept_id", agent.getDept_id()).set("default",
				false);
		mongoTemplate.updateMulti(cqb.getQuery(), cqb.getUpdate(), AgentDoc.class);

		CommonMongoQueryBuilder cqb2 = new CommonMongoQueryBuilder().whereId(agent_id).set("default", true);
		mongoTemplate.updateMulti(cqb2.getQuery(), cqb2.getUpdate(), AgentDoc.class);
	}

	public void updateDepartmentDefault(String deptId) {
		updateMulti(new CommonMongoQueryBuilder().whereAll().set("default", false), AgentDoc.class);
		updateMulti(new CommonMongoQueryBuilder().whereId(deptId).set("default", true), AgentDoc.class);
	}

}
