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
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class AgentStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentStore.class);

    @Autowired
    MongoTemplate mongoTemplate;

    public void updateMulti(CommonMongoQueryBuilder builder, Class<?> entityClass) {
	mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), entityClass);
    }

    public void updateFirst(CommonMongoQueryBuilder builder, Class<?> entityClass) {
	mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), entityClass);
    }

    public List<AgentDoc> findAll() {
	return mongoTemplate.findAll(AgentDoc.class);
    }

    public List<AgentDoc> findAllActive() {
	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().where("isactive", "Y");
	return mongoTemplate.find(builder.getQuery(), AgentDoc.class);
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

    public void updateAgentActive(String agentId, String status) {
	boolean isEnabled = "Y".equalsIgnoreCase(status);
	CommonMongoQueryBuilder cqb2 = new CommonMongoQueryBuilder().whereId(agentId).set("isEnabled", isEnabled)
		.set("isactive", status);
	updateFirst(cqb2, AgentDoc.class);
    }

    public void updateAgentDefault(String agentId) {
	AgentDoc agent = findById(agentId);
	if (!agent.isDefaultValue()) {
	    CommonMongoQueryBuilder cqb = new CommonMongoQueryBuilder().where("dept_id", agent.getDept_id())
		    .set("isDefaultValue", false);
	    updateMulti(cqb, AgentDoc.class);
	}
	CommonMongoQueryBuilder cqb2 = new CommonMongoQueryBuilder().whereId(agentId).set("isDefaultValue",
		!agent.isDefaultValue());
	updateMulti(cqb2, AgentDoc.class);
    }

    public void updateDepartmentDefault(String deptId) {
	DepartmentDoc dept = findDepartmentById(deptId);
	if (!dept.isDefaultValue()) {
	    updateMulti(new CommonMongoQueryBuilder().whereAll().set("isDefaultValue", false), DepartmentDoc.class);
	}
	updateMulti(new CommonMongoQueryBuilder().whereId(deptId).set("isDefaultValue", !dept.isDefaultValue()),
		DepartmentDoc.class);
    }

    public void save(AgentDoc agent) {
	agent.setAuthKey(PostManUtil.UNIQUE_API_KEY());
	mongoTemplate.save(agent);
    }

}
