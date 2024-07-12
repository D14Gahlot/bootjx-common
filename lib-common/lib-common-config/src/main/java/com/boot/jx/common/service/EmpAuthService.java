package com.boot.jx.common.service;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.CDNBuilder;
import com.boot.jx.common.doc.AgentDoc;
import com.boot.jx.common.doc.DepartmentDoc;
import com.boot.jx.common.doc.UserAuthTokenDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.dto.DepartmentResponseAuthDto;
import com.boot.jx.common.dto.UserAuthToken;
import com.boot.jx.common.store.AgentStore;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.channel.OAClient;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.config.ChannelConfigDoc;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.Random;
import com.boot.utils.TimeUtils.TimeUnits;

@Component
public class EmpAuthService {

	private static final Logger LOGGER = LoggerService.getLogger(CDNBuilder.class);

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private PostManClient postManClient;

	@Autowired
	private AgentStore agentStore;

	@Value("${mry.superadmin.user}")
	private String superAdminUser;

	@Value("${mry.superadmin.pass}")
	private String superAdminPass;

	@Value("${mry.duperadmin.email}")
	private String duperAdminEmail;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private OAClient oaClient;

	private AgentDoc validateAgent(String username, String email, String passsword) throws NoSuchAlgorithmException {
		if (ArgUtil.isEmpty(passsword)) {
			return null;
		}
		AgentDoc agent = getAgentByCodeAndStatus(username, email, "Y");
		String passwordMd5 = CryptoUtil.getMD5Hash(passsword);
		String passwordSHA1 = CryptoUtil.getSHA1Hash(passsword);
		String passwordSHA256 = CryptoUtil.getSHA2Hash(passsword);
		if (ArgUtil.is(agent)) {
			if (ArgUtil.isEqual(passwordSHA256, superAdminPass)
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

	public AgentResponseAuthDto loginAgent(String username, String passsword) throws NoSuchAlgorithmException {
		AgentDoc agent = validateAgent(username, username, passsword);
		if (ArgUtil.is(agent)) {
			fixAppModules(agent);
			DepartmentDoc dept = agentStore.findDepartmentById(agent.getDept_id());
			return new AgentResponseAuthDto().importFrom(agent).dept(new DepartmentResponseAuthDto().importFrom(dept));
		}
		return null;
	}

	public AgentResponseAuthDto loginByDomainToken(UserAuthToken userAuthToken) throws NoSuchAlgorithmException {
		return loginByDomainToken(userAuthToken.getDomainUser(), userAuthToken.getDomainUserEmail(),
				userAuthToken.getDomainName(), userAuthToken.getDomainId(), userAuthToken.getDomainToken());
	}

	public AgentResponseAuthDto loginByDomainToken(String username, String userEmail, String domainName,
			String domainId, String domainToken) throws NoSuchAlgorithmException {
		if (!ArgUtil.areEqual(AppContextUtil.getTenant(), domainName)) {
			LOGGER.info("DOMAIN MISMATCH {}<>{}", AppContextUtil.getTenant(), domainName);
			return null;
		}

		AgentDoc agent = getAgentByCodeAndStatus(username, userEmail, "Y");
		if (!ArgUtil.is(agent)) {
			LOGGER.info("NO USER FOUND {}", username);
			return null;
		}

		HashBuilder builder = getHashBuilder(username, userEmail, AppContextUtil.getTenant(), domainId,
				agent.getAuthKey());

		if (ArgUtil.is(agent) && builder.validate(domainToken)) {
			fixAppModules(agent);
			DepartmentDoc dept = agentStore.findDepartmentById(agent.getDept_id());
			return new AgentResponseAuthDto().importFrom(agent).dept(new DepartmentResponseAuthDto().importFrom(dept));
		}
		return null;
	}

	public void fixAppModules(AgentDoc agent) {
		Set<String> appModules = new HashSet<String>(agent.appModules());

		if (agent.isAdmin() && !appModules.contains(PMConstants.APP_MODULES.ADMIN.name())) {
			appModules.add(PMConstants.APP_MODULES.ADMIN.name());
		}

		if (agent.getIsEnabled() && !appModules.contains(PMConstants.APP_MODULES.AGENT.name())) {
			appModules.add(PMConstants.APP_MODULES.AGENT.name());
		}
		agent.setAppModules(new ArrayList<String>(appModules));
	}

	public boolean resetPassword(String username, boolean admin) throws NoSuchAlgorithmException {
		AgentDoc agent = getAgentByCodeAndStatus(username, username, "Y");
		if (!ArgUtil.is(agent)) {
			return false;
		}
		agent.setAgent_otp(Random.randomAlphaNumeric(10));
		mongoTemplate.save(agent);

		String app = admin ? "admin" : "agent";
		String domain = AppContextUtil.getTenant();

		postManClient.send(new MessageBox().push(new Email().to(agent.getAgent_email()).template("agent-reset-pass")
				.put("otp", agent.getAgent_otp()).put("username", agent.getAgent_code())
				.put("logo", pmEnvironment.keyEntry("mry.prop.logo.bg-x-icon").asString())
				.put("website", pmEnvironment.keyEntry("mry.prop.service.website").asString())
				.put("service", pmEnvironment.keyEntry("mry.prop.service.name").asString())
				.put("serviceDomain", pmEnvironment.keyEntry("mry.prop.service.domain").asString())
				.put("link", String.format(
						"https://%s.%s/front/auth/resetpass?page=setpass&username=%s&token=%s&stamp=0&domain=%s",
						domain, pmEnvironment.keyEntry("mry.prop.service.domain").asString(), agent.getAgent_code(),
						agent.getAgent_otp(), domain))
				.put("tnt", domain).put("panel", app).put("contactName", agent.getAgent_name())));

		return true;
	}

	private AgentDoc getAgentByCodeAndStatus(String username, String email, String status) {
		if (ArgUtil.areEqual(superAdminUser, username)) {
			AgentDoc agentLocal = new AgentDoc();
			agentLocal.setAgent_code(username);
			agentLocal.setAgent_email(email);
			agentLocal.setAgent_password(superAdminPass);
			agentLocal.setAuthKey(appConfig.prop("mry.app.login.key"));

			agentLocal.setAdmin(true);
			agentLocal.setSuperAdmin(true);
			if (ArgUtil.areEqual(duperAdminEmail, email)) {
				agentLocal.setDuperAdmin(true);
			}
			return agentLocal;
		}

		Query query2 = new Query();
		query2.addCriteria(Criteria.where("isactive").is(status).orOperator(Criteria.where("agent_code").is(username),
				Criteria.where("agent_code").regex("^" + username + "$", "i"), Criteria.where("agent_email").is(email),
				Criteria.where("agent_email").regex("^" + email + "$", "i")));
		// System.out.println("" + query2.toString());
		AgentDoc agent = CollectionUtil.getOne(mongoTemplate.find(query2, AgentDoc.class));

//		if (admin && ArgUtil.is(agent)) {
//			return agent.isAdmin() ? agent : null;
//		}
		return agent;
	}

	public boolean setPassword(String username, String passsword, String newpasssword) throws NoSuchAlgorithmException {
		AgentDoc agent = validateAgent(username, username, passsword);
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
			x.setMessage("Password reset email sent");
		} else {
			x.data().put("success", false);
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username is incorrect");
		}
		return x;
	}

	public ApiResponse<Map<String, Object>, String> agentSetPass(String username, String password, String newpassword)
			throws NoSuchAlgorithmException {
		ApiResponse<Map<String, Object>, String> x = ApiResponse
				.buildData(MapBuilder.map().put("success", true).toMap(), "success");
		if (setPassword(username, password, newpassword)) {
			x.setStatusKey("SUCCESS");
		} else {
			x.data().put("success", false);
			x.setMeta("error");
			x.setStatusKey("ERROR");
			x.setMessage("Username is incorrect");
		}
		return x;
	}

	public ApiResponse<Map<String, Object>, AgentResponseAuthDto> empLogin(String username, String password)
			throws NoSuchAlgorithmException {
		AgentResponseAuthDto agent = loginAgent(username, password);
		if (ArgUtil.is(agent)) {
			return ApiResponse.buildData(MapBuilder.map().put("success", true).toMap(), agent).statusKey("SUCCESS");
		} else {
			return ApiResponse.buildData(MapBuilder.map().put("success", false).toMap(), agent).statusKey("ERROR")
					.message("Username or Password is incorrect");
		}
	}

	public UserAuthToken createAgentLoginToken(String username, String email, String password, String domainName,
			String domainId, String app, String event) throws NoSuchAlgorithmException {
		UserAuthToken userLoginToken = new UserAuthToken();
		AgentDoc agent = validateAgent(username, email, password);
		if (ArgUtil.is(agent)) {
			HashBuilder builder = getHashBuilder(agent.getAgent_code(), agent.getAgent_email(), domainName, domainId,
					agent.getAuthKey());
			userLoginToken.setDomainName(domainName);
			userLoginToken.setDomainId(domainId);
			userLoginToken.setDomainToken(builder.toHMAC().output());
			userLoginToken.setDomainUser(agent.getAgent_code());
			userLoginToken.setDomainUserEmail(agent.getAgent_email());
			userLoginToken.setDomainUserPhone(agent.getPhone());
			userLoginToken.setApp(app);
			userLoginToken.setEvent(event);
		} else {
			ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("password")
					.codeKey("ValidCredentials").description("Invalid Email or Password"));
		}
		return userLoginToken;
	}

	public UserAuthToken createSuperLoginToken(String username, String email, String domainName, String domainId,
			String app) throws NoSuchAlgorithmException {
		UserAuthToken userLoginToken = new UserAuthToken();
		String authKey = appConfig.prop("mry.app.login.key");
		HashBuilder builder = getHashBuilder(username, email, domainName, domainId, authKey);
		userLoginToken.setDomainName(domainName);
		userLoginToken.setDomainId(domainId);
		userLoginToken.setDomainToken(builder.toHMAC().output());
		userLoginToken.setDomainUser(username);
		userLoginToken.setDomainUserEmail(email);
		userLoginToken.setApp(app);
		return userLoginToken;
	}

	public boolean sendOTP(UserAuthToken loginToken) {

		PMConfigurationObject mfaEnabled = pmEnvironment.keyEntry(PMConstants.PROPERTIES.POSTMAN_AGENT_2FA_ENABLED);

		if (!mfaEnabled.exists() || !mfaEnabled.asBoolean()) {
			return false;
		}

		OTPDetails otpDetails = OTPUtils.genrateBasicOTP(loginToken.getDomainUser(), loginToken.getApp());

		PMConfigurationObject otpChannel = pmEnvironment.keyEntry(PMConstants.PROPERTIES.POSTMAN_AGENT_2FA_CHANNEL);
		// .asString("oa:mehery");

		if (!otpChannel.exists()) {
			return false;
		}

		ChannelConfig channel = pmEnvironment.config().channel(otpChannel.asString());

		if (!ArgUtil.is(channel)) {
			channel = mongoTemplate.findById(otpChannel.asString(), ChannelConfigDoc.class, "CONFIG_CHANNEL_X");
		}

		if (!ArgUtil.is(channel) || !ArgUtil.is(channel.getOa())) {
			return false;
		}

		OutboxMessage ob = new OutboxMessage();
		ob.contact().setPhone(loginToken.getDomainUserPhone());
		ob.setTemplateExt(new HSMTemplate3rdParty().code("login_otp"));
		ob.model().put("prefix", otpDetails.getPrefix());
		ob.model().put("value", otpDetails.getOtp());
		ob.model().put("data",
				MapModel.createInstance().put("panel", ArgUtil.nonEmpty(loginToken.getApp(), Constants.BLANK)).toMap());

		oaClient.sendMessage(channel, ob);

		// Details to SHOW/MASK to UI
		loginToken.setOtpPrefix(otpDetails.getPrefix());
		loginToken.setOtpNounce(otpDetails.getYin());
		loginToken.setDomainToken(null);

		// Details to SAVE in DB
		UserAuthTokenDoc loginDoc = EntityDtoUtil.dtoToEntity(loginToken, new UserAuthTokenDoc());
		loginDoc.setOtpNounce(otpDetails.getYang());
		loginDoc.setOtpHash(otpDetails.getHash());
		mongoTemplate.save(loginDoc);

		// Details to SHOW/MASK to UI
		loginToken.setTokenId(loginDoc.getTokenId());
		return true;
	}

	private HashBuilder getHashBuilder(String username, String email, String domainName, String domainId,
			String authKey) {
		String secret = appConfig.prop("mry.app.login.secret");
		HashBuilder builder = new HashBuilder().interval(TimeUnits.DAYS.toSeconds(30)).secret(secret)
				.message(String.format("%s@%s:%s#%s=%s", username, domainName, domainId, authKey, email));
		return builder;
	}
}
