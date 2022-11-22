package com.boot.jx.common.store;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.mongo.CommonDocInterfaces.IMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQB;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class AgentStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentStore.class);

	@Autowired
	CommonMongoTemplate mongoTemplate;

	public void logAgentUpdate(AgentDoc agent) {
		mongoTemplate.log(agent, "updated");
	}

	public void logAgentUpdate(String id) {
		AgentDoc agent = findById(id);
		logAgentUpdate(agent);
	}

	public void updateMulti(IMongoQueryBuilder<?> builder, Class<?> entityClass) {
		mongoTemplate.updateMulti(builder.getQuery(), builder.getUpdate(), entityClass);
	}

	public void updateFirst(IMongoQueryBuilder<?> builder, Class<?> entityClass) {
		mongoTemplate.updateFirst(builder.getQuery(), builder.getUpdate(), entityClass);
	}

	public List<AgentDoc> findAll() {
		return mongoTemplate.findAll(AgentDoc.class);
	}

	public List<AgentDoc> findAllAgents(boolean includeInActive) {
		MongoQueryBuilder<AgentDoc> builder = MongoQueryBuilder.collection(AgentDoc.class);
		if (!includeInActive) {
			builder.where("isactive", "Y");
		}
		builder.sortBy("agent_code");
		return mongoTemplate.find(builder);
	}

	public List<AgentDoc> findAllActive() {
		return findAllAgents(false);
	}

	public AgentDoc findById(String agentId) {
		MongoQueryBuilder<AgentDoc> builder = MongoQueryBuilder.collection(AgentDoc.class).whereId(agentId);
		return mongoTemplate.findOne(builder.getQuery(), AgentDoc.class);
	}

	public AgentDoc findByCode(String agentCode) {
		MongoQueryBuilder<AgentDoc> builder = MongoQueryBuilder.collection(AgentDoc.class).where("agent_code",
				agentCode);
		return mongoTemplate.findOne(builder.getQuery(), AgentDoc.class);
	}

	public List<DepartmentDoc> findDepartmentAll(boolean showInActive) {
		if (showInActive) {
			return mongoTemplate.findAll(DepartmentDoc.class);
		}
		MongoQueryBuilder<Object> builder = new CommonMongoQueryBuilder().where("isactive", "Y");
		return mongoTemplate.find(builder.getQuery(), DepartmentDoc.class);
	}

	public List<DepartmentDoc> findDepartmentAll() {
		return findDepartmentAll(false);
	}

	public DepartmentDoc findDepartmentById(String deptId) {
		MongoQueryBuilder<DepartmentDoc> builder = MongoQueryBuilder.collection(DepartmentDoc.class).whereId(deptId);
		return mongoTemplate.findOne(builder.getQuery(), DepartmentDoc.class);
	}

	public DepartmentDoc findDepartmentByCode(String deptCode) {
		MQB<DepartmentDoc> builder = MQB.select(DepartmentDoc.class).where("dept_code",deptCode);
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
		MQB<AgentDoc> cqb2 = MQB.select(AgentDoc.class).whereId(agentId).set("isEnabled", isEnabled).set("isactive",
				status);
		updateFirst(cqb2, AgentDoc.class);
		logAgentUpdate(agentId);
	}

	public void updateAgentDefault(String agentId) {
		AgentDoc agent = findById(agentId);
		if (!agent.isDefaultValue()) {
			MongoQueryBuilder<?> cqb = new CommonMongoQueryBuilder().where("dept_id", agent.getDept_id())
					.set("isDefaultValue", false);
			updateMulti(cqb, AgentDoc.class);
		}
		MongoQueryBuilder<AgentDoc> cqb2 = MongoQueryBuilder.collection(AgentDoc.class).whereId(agentId)
				.set("isDefaultValue", !agent.isDefaultValue());
		updateMulti(cqb2, AgentDoc.class);
		logAgentUpdate(agent);
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
		// mongoTemplate.save(agent);
		mongoTemplate.saveAndAudit(agent, ArgUtil.is(agent.getId()));
	}

}
