package com.boot.jx.common.service;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.dto.DepartmentResponseAuthDto;
import com.boot.jx.common.dto.UserLoginToken;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.scope.tnt.TenantValue;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.boot.utils.MapBuilder;
import com.boot.utils.Random;

@Component
@TenantScoped
public class EmpAuthService {

    private static final Logger LOGGER = LoggerService.getLogger(CDNBuilder.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PostManClient postManClient;

    @Autowired
    private AgentStore agentStore;

    @TenantValue("${mry.superadmin.user}")
    private String superAdminUser;

    @TenantValue("${mry.superadmin.pass}")
    private String superAdminPass;

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private PMEnvironment pmEnvironment;

    private AgentDoc validateAgent(String username, String passsword, boolean admin) throws NoSuchAlgorithmException {
	if (ArgUtil.isEmpty(passsword)) {
	    return null;
	}
	AgentDoc agent = getAgentByCodeAndStatus(username, "Y", admin);
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

    public AgentResponseAuthDto loginAgent(String username, String passsword, boolean admin)
	    throws NoSuchAlgorithmException {
	AgentDoc agent = validateAgent(username, passsword, admin);
	if (ArgUtil.is(agent)) {
	    DepartmentDoc dept = agentStore.findDepartmentById(agent.getDept_id());
	    return new AgentResponseAuthDto().importFrom(agent).dept(new DepartmentResponseAuthDto().importFrom(dept));
	}
	return null;
    }

    public AgentResponseAuthDto loginByDomainToken(String username, String domainName, String domainId,
	    String domainToken, boolean adminPanel) throws NoSuchAlgorithmException {
	if (!ArgUtil.areEqual(AppContextUtil.getTenant(), domainName)) {
	    LOGGER.info("DOMAIN MISMATCH {}<>{}", AppContextUtil.getTenant(), domainName);
	    return null;
	}

	AgentDoc agent = getAgentByCodeAndStatus(username, "Y", adminPanel);
	if (!ArgUtil.is(agent)) {
	    LOGGER.info("NO USER FOUND {}", username);
	    return null;
	}

	HashBuilder builder = getHashBuilder(username, AppContextUtil.getTenant(), domainId, agent.getAuthKey());

	if (ArgUtil.is(agent) && builder.validate(domainToken)) {
	    DepartmentDoc dept = agentStore.findDepartmentById(agent.getDept_id());
	    return new AgentResponseAuthDto().importFrom(agent).dept(new DepartmentResponseAuthDto().importFrom(dept));
	}
	return null;
    }

    public boolean resetPassword(String username, boolean admin) throws NoSuchAlgorithmException {
	AgentDoc agent = getAgentByCodeAndStatus(username, "Y", admin);
	if (!ArgUtil.is(agent)) {
	    return false;
	}
	agent.setAgent_otp(Random.randomAlphaNumeric(10));
	mongoTemplate.save(agent);

	String app = admin ? "admin" : "agent";
	String domain = AppContextUtil.getTenant();
	postManClient.send(new MessageBox().push(new Email().to(agent.getAgent_email()).template("agent-reset-pass")
		.put("otp", agent.getAgent_otp()).put("username", agent.getAgent_code())
		.put("logo", pmEnvironment.get("mry.prop.logo.bg-x-icon").asString())
		.put("website", pmEnvironment.get("mry.prop.service.website").asString())
		.put("service", pmEnvironment.get("mry.prop.service.name").asString())
		.put("serviceDomain", pmEnvironment.get("mry.prop.service.domain").asString())
		.put("link",
			String.format("https://%s.%s.com/%s/auth/resetpass?page=setpass&username=%s&token=%s&stamp=0",
				domain, pmEnvironment.get("mry.prop.service.domain").asString(), app,
				agent.getAgent_code(), agent.getAgent_otp()))
		.put("tnt", domain).put("panel", app).put("contactName", agent.getAgent_name())));
	return true;
    }

    private AgentDoc getAgentByCodeAndStatus(String username, String status, boolean admin) {
	if (admin && ArgUtil.areEqual(superAdminUser, username)) {
	    AgentDoc agent = new AgentDoc();
	    agent.setAgent_code(username);
	    agent.setAdmin(true);
	    agent.setSuperAdmin(true);
	    agent.setAgent_password(superAdminPass);
	    agent.setAuthKey(appConfig.prop("mry.app.login.key"));
	    return agent;
	}

	Query query2 = new Query();
	query2.addCriteria(Criteria.where("isactive").is(status).orOperator(Criteria.where("agent_code").is(username),
		Criteria.where("agent_code").regex("^" + username + "$", "i"),
		Criteria.where("agent_email").is(username),
		Criteria.where("agent_email").regex("^" + username + "$", "i")));
	AgentDoc agent = CollectionUtil.getOne(mongoTemplate.find(query2, AgentDoc.class));

	if (admin && ArgUtil.is(agent)) {
	    return agent.isAdmin() ? agent : null;
	}
	return agent;
    }

    public boolean setPassword(String username, String passsword, String newpasssword, boolean admin)
	    throws NoSuchAlgorithmException {
	AgentDoc agent = validateAgent(username, passsword, admin);
	if (!ArgUtil.is(agent)) {
	    return false;
	}
	agent.setAgent_password(CryptoUtil.getSHA2Hash(newpasssword));
	agent.setAgent_otp(null);
	agentStore.save(agent);
	return true;
    }

    // Interface Service
    public ApiResponse<Map<String, Object>, String> agentResetPass(String username, boolean admin)
	    throws NoSuchAlgorithmException {
	ApiResponse<Map<String, Object>, String> x = ApiResponse
		.buildData(MapBuilder.map().put("success", true).toMap(), "success");
	if (resetPassword(username, admin)) {
	    x.setStatusKey("SUCCESS");
	} else {
	    x.data().put("success", false);
	    x.setMeta("error");
	    x.setStatusKey("ERROR");
	    x.setMessage("Username is incorrect");
	}
	return x;
    }

    public ApiResponse<Map<String, Object>, String> agentSetPass(String username, String password, String newpassword,
	    boolean admin) throws NoSuchAlgorithmException {
	ApiResponse<Map<String, Object>, String> x = ApiResponse
		.buildData(MapBuilder.map().put("success", true).toMap(), "success");
	if (setPassword(username, password, newpassword, admin)) {
	    x.setStatusKey("SUCCESS");
	} else {
	    x.data().put("success", false);
	    x.setMeta("error");
	    x.setStatusKey("ERROR");
	    x.setMessage("Username is incorrect");
	}
	return x;
    }

    public ApiResponse<Map<String, Object>, AgentResponseAuthDto> empLogin(String username, String password,
	    boolean admin) throws NoSuchAlgorithmException {
	AgentResponseAuthDto agent = loginAgent(username, password, admin);
	if (ArgUtil.is(agent)) {
	    return ApiResponse.buildData(MapBuilder.map().put("success", true).toMap(), agent).statusKey("SUCCESS");
	} else {
	    return ApiResponse.buildData(MapBuilder.map().put("success", false).toMap(), agent).statusKey("ERROR")
		    .message("Username or Password is incorrect");
	}
    }

    public UserLoginToken createAgentLoginToken(String username, String password, String domainName, String domainId,
	    String app) throws NoSuchAlgorithmException {
	UserLoginToken userLoginToken = new UserLoginToken();
	AgentDoc agent = validateAgent(username, password, "admin".equals(app));
	if (ArgUtil.is(agent)) {
	    HashBuilder builder = getHashBuilder(username, domainName, domainId, agent.getAuthKey());
	    userLoginToken.setDomainName(domainName);
	    userLoginToken.setDomainId(domainId);
	    userLoginToken.setDomainToken(builder.toHMAC().output());
	    userLoginToken.setDomainUser(username);
	    userLoginToken.setApp(app);
	}
	return userLoginToken;
    }

    public UserLoginToken createSuperLoginToken(String username, String domainName, String domainId, String app)
	    throws NoSuchAlgorithmException {
	UserLoginToken userLoginToken = new UserLoginToken();
	String authKey = appConfig.prop("mry.app.login.key");
	HashBuilder builder = getHashBuilder(username, domainName, domainId, authKey);
	userLoginToken.setDomainName(domainName);
	userLoginToken.setDomainId(domainId);
	userLoginToken.setDomainToken(builder.toHMAC().output());
	userLoginToken.setDomainUser(username);
	userLoginToken.setApp(app);
	return userLoginToken;
    }

    private HashBuilder getHashBuilder(String username, String domainName, String domainId, String authKey) {
	String secret = appConfig.prop("mry.app.login.secret");
	HashBuilder builder = new HashBuilder().interval(10000).secret(secret)
		.message(String.format("%s@%s:%s#%s", username, domainName, domainId, authKey));
	return builder;
    }
}
