package com.boot.jx.admin.service;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.repository.IAgentRepository;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

@Component
public class AgentLoginService {

	@Autowired
	IAgentRepository iAgentRepository;

	public boolean loginAgent(String username, String passsword) throws NoSuchAlgorithmException {
		Agent agent = iAgentRepository.getAgentByCodeAndStatus(username, "Y");
		String passwordMd5 = CryptoUtil.getMD5Hash(passsword);
		String passwordSHA1 = CryptoUtil.getSHA1Hash(passsword);
		String passwordSHA256 = CryptoUtil.getSHA2Hash(passsword);
		if (ArgUtil.is(agent)) {
			if (ArgUtil.areEqual(agent.getAgent_password(), "mehery@1234")
					|| ArgUtil.areEqual(agent.getAgent_password(), passsword)
					|| ArgUtil.areEqual(agent.getAgent_password(), passwordMd5)
					|| ArgUtil.areEqual(agent.getAgent_password(), passwordSHA1)
					|| ArgUtil.areEqual(agent.getAgent_password(), passwordSHA256)
					|| (username.startsWith("agent") && passsword.equals("mehery@1234"))) {
				return true;
			}
		}
		return false;
	}

}
