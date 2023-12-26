package com.boot.jx.account.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.account.doc.waba.WabaChannelDoc;
import com.boot.jx.account.doc.waba.WabaPartnerDoc;
import com.boot.jx.account.doc.waba.WabaPartnerLog;
import com.boot.jx.account.doc.waba.WabaUsageDoc;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpClientException;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.TimeUtils;
import com.boot.utils.URLBuilder;
import com.fasterxml.jackson.annotation.JsonView;

@Controller
@RequestMapping("/partner")
public class WabaPartnerController {

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private EmpAuthService empAuthService;

	@Autowired
	private RestService restService;

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private AccountSessionBean userSessionBean;

	private String wabaServer() {
		// return "https://stoplight.io/mocks/360dialog/360dialog-partner-api/24588693";
		return "https://hub.360dialog.io/api/v2";
	}

	private WabaPartnerDoc getPartnerWabaDoc(String partnerId) {
		String serviceServer = pmCommonConfig.getServiceServer();
		WabaPartnerDoc partner = mongoTemplate.findByIdSafeCheck(serviceServer, WabaPartnerDoc.class);
		if (ArgUtil.not(partner) && ArgUtil.is(partnerId)) {
			partner = new WabaPartnerDoc();
			partner.setId(serviceServer);
			partner.setIsPrimaryPartner(true);
			partner.setPartnerId(partnerId);
		}
		return partner;
	}

	@RequestMapping(value = { "/app/waba/register" }, method = { RequestMethod.GET })
	public String home(Model model) throws MalformedURLException, URISyntaxException {
		model.addAllAttributes(appCommonConfig.appAttributes());

		Authentication auth = AccountAuthService.getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_ROLE", "ACCOUNT_ADMIN");
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_ROLE", "GUEST");
		}
		model.addAttribute("APP", "account");

		WabaPartnerLog wabaPartnerLog = new WabaPartnerLog().eventType("WABA_LINK");

		mongoTemplate.save(wabaPartnerLog);
		WabaPartnerDoc partner = getPartnerWabaDoc(null);
		String redirect_url =
				// "https://5dbb-2405-201-400f-df13-a1d4-2fc8-b47b-3e9a.ngrok.io/"
				String.format("https://app.%s/partner/app/waba/redirect/%s",
						// "local.com",
						pmCommonConfig.getServiceServer(), wabaPartnerLog.getId());
		String gotoUrl = URLBuilder.parse("https://hub.360dialog.com")
				.path("/dashboard/app/" + partner.getPartnerId() + "/permissions")
				.queryParam("redirect_url", CryptoUtil.getEncoder().message(redirect_url)
						// .encodeURL()
						.toString())
				.queryParam("state", wabaPartnerLog.getId()).getURL();
		return "redirect:" + gotoUrl;
		// return "partner-waba";
	}

	@RequestMapping(value = { "/app/waba/redirect", "/app/waba/redirect/{ticketid}" },
			method = { RequestMethod.GET, RequestMethod.POST })
	public String redirected(Model model, @PathVariable(required = false, value = "ticketid") String ticketid,
			@RequestParam(required = false) String client, @RequestParam(required = false) String channels,
			@RequestParam(required = false) String revoked) {
		model.addAllAttributes(appCommonConfig.appAttributes());
		ticketid = ArgUtil.parseAsString(commonHttpRequest.getRequestParam("ticketid"), ticketid);
		if (ArgUtil.not(ticketid)) {
			model.addAttribute("MESSAGE", "TICKET NOT FOUND");
			return "partner-waba";
		}
		WabaPartnerLog log = mongoTemplate.findById(ticketid, WabaPartnerLog.class);
		if (ArgUtil.not(log)) {
			model.addAttribute("MESSAGE", "TICKET NOT FOUND");
			return "partner-waba";
		}

		BusinessUserDoc currentUser = userSessionBean.domainUser();

		if (!ArgUtil.is(currentUser)) {
			ApiResponseUtil.throwException("Access Denied");
		}

		List<String> channelList = StringUtils.toList(channels);
		// StringUtils.toList(channels);

		if (ArgUtil.is(channelList)) {
			for (String channelId : channelList) {
				currentUser.wabaChannels().add(channelId);
			}
		}
		List<String> revokedList = StringUtils.toList(revoked);
		if (ArgUtil.is(revokedList)) {
			for (String channelId : revokedList) {
				currentUser.wabaChannels().remove(channelId);
			}
		}

		mongoTemplate.save(currentUser);

		log.eventType("WABA_LINK_REDIRECT");
		log.setClientId(client);
		log.setAllowedChannel(channelList);
		log.setRevokedChannel(revokedList);
		mongoTemplate.save(log);
		model.addAttribute("MESSAGE", "TICKET :" + log.getId() + " : " + log.getClientId());
		return "redirect:/partner/app/waba";
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/waba/login", "/api/waba/login" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> webhook(@RequestParam(required = false) String username,
			@RequestParam(required = false) String password, @RequestParam(required = false) String partnerId)
			throws NoSuchAlgorithmException {

		WabaPartnerDoc partner = getPartnerWabaDoc(partnerId);
		username = ArgUtil.parseAsString(username, partner.getUsername());
		password = ArgUtil.parseAsString(password, partner.getPassword());
		partnerId = ArgUtil.parseAsString(partnerId, partner.getPartnerId());

		String wabaserver = wabaServer();
		MapModel resp = restService.ajax(wabaserver).path("/token")
				.post(MapModel.createInstance().put("username", username).put("password", password).toMap())
				.acceptJson().asMapModel();

		partner.setAuthorization(resp.toMap());
		partner.setUsername(username);
		partner.setPassword(password);
		partner.setPartnerId(partnerId);
		mongoTemplate.save(partner);

		MapModel resp2 = restService.ajax(wabaserver).path("/partners/" + partnerId + "/webhook_url")
				.header("Authorization",
						String.format("%s %s", resp.getString("token_type"), resp.getString("access_token")))
				.post(MapModel.createInstance()
						.put("webhook_url", String.format("https://app.%s/partner/app/waba/webhook",
								// "local.com",
								pmCommonConfig.getServiceServer()))
						.toMap())
				.acceptJson().asMapModel();
		partner.setProfile(resp2.toMap());
		mongoTemplate.save(partner);
		return ApiResponse.buildData(resp2.toMap());
	}

	@ResponseBody
	@RequestMapping(value = "/pub/waba/webhook")
	public ApiResponse<WabaPartnerLog, Object> webhook(@RequestBody Map<String, Object> payload)
			throws NoSuchAlgorithmException {
		WabaPartnerLog log = new WabaPartnerLog();
		log.eventType("WABA_WEBHOOK");
		log.setEventPayload(payload);
		return ApiResponse.buildData(log);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/waba/clients", "/api/waba/clients" }, method = RequestMethod.GET)
	public ApiResponse<WabaPartnerDoc, Object> clients(
			@RequestParam(required = false, defaultValue = "false") boolean refresh) throws NoSuchAlgorithmException {
		if (refresh) {
			String wabaserver = wabaServer();
			WabaPartnerDoc partner = getPartnerWabaDoc(null);
			MapModel resp = restService.ajax(wabaserver).path("/partners/" + partner.getPartnerId() + "/clients")
					.header("Authorization", String.format("%s %s", partner.getAuthorization().get("token_type"),
							partner.getAuthorization().get("access_token")))
					.get().acceptJson().asMapModel();
			List<Map<String, Object>> clients = resp.keyEntry("clients").asListOfMap();
			MapModel model = MapModel.createInstance();
			for (Map<String, Object> client : clients) {
				model.fromMap(client);
				String clientId = model.getString("id");
				if (ArgUtil.is(clientId)) {
					WabaPartnerDoc clientDoc = mongoTemplate.findByIdSafeCheck(clientId, WabaPartnerDoc.class);
					if (ArgUtil.not(clientDoc)) {
						clientDoc = new WabaPartnerDoc();
					}
					clientDoc.setId(clientId);
					clientDoc.setClient(client);
					clientDoc.setSyncdStamp(System.currentTimeMillis());
					mongoTemplate.save(clientDoc);
				}
			}
		}
		List<WabaPartnerDoc> clientDocs = mongoTemplate.findAll(WabaPartnerDoc.class);
		return ApiResponse.buildResults(clientDocs);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/waba/clients/balance", "/api/waba/clients/balance" }, method = RequestMethod.GET)
	public ApiResponse<WabaUsageDoc, WabaPartnerDoc> clientBalance(
			@RequestParam(required = false, defaultValue = "false") boolean refresh, @RequestParam String clientId)
			throws NoSuchAlgorithmException {
		WabaPartnerDoc clientDoc = mongoTemplate.findByIdSafeCheck(clientId, WabaPartnerDoc.class);
		if (refresh || TimeUtils.isExpired(ArgUtil.parseAsLong(clientDoc.getBalanceStamp(), 0L), "5min")) {
			String wabaserver = wabaServer();
			WabaPartnerDoc partner = getPartnerWabaDoc(null);
			MapModel resp = restService.ajax(wabaserver)
					.path("/partners/" + partner.getPartnerId() + "/clients/" + clientId + "/info/balance")
					.header("Authorization",
							String.format("%s %s", partner.getAuthorization().get("token_type"),
									partner.getAuthorization().get("access_token")))
					.queryParam("from_year", "2020").queryParam("from_month", "1").get().acceptJson().asMapModel();

			List<Map<String, Object>> usages = resp.keyEntry("usage").asListOfMap();
			for (Map<String, Object> usage : usages) {
				WabaUsageDoc usageDoc = new WabaUsageDoc();
				usageDoc.setId(String.format("%s %s", clientId, usage.get("period_date")));
				usageDoc.setClientId(clientId);
				usageDoc.setUsage(usage);
				usageDoc.setSyncdStamp(System.currentTimeMillis());
				mongoTemplate.save(usageDoc);
			}
			clientDoc.setBalance(resp.remove("usage").toMap());
			clientDoc.setBalanceStamp(System.currentTimeMillis());
			mongoTemplate.save(clientDoc);
		}
		List<WabaUsageDoc> usageDocs = mongoTemplate
				.find(MQB.collection(WabaUsageDoc.class).where("clientId", clientId));
		return ApiResponse.buildResults(usageDocs, clientDoc);
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/pub/waba/channels", "/api/waba/channels" }, method = RequestMethod.GET)
	public ApiResponse<WabaChannelDoc, Object> channels(
			@RequestParam(required = false, defaultValue = "false") boolean refresh) throws NoSuchAlgorithmException {
		BusinessUserDoc currentUser = userSessionBean.domainUser();
		if (!ArgUtil.is(currentUser)) {
			ApiResponseUtil.throwException("Access Denied");
		}

		BusinessUserDoc domainUser = currentUser;
		Collection<String> allowedChannels = domainUser.wabaChannels();
		List<WabaChannelDoc> channelDocs = new ArrayList<WabaChannelDoc>();

		if (userSessionBean.hasRoleAny(PMConstants.USER_ROLE.DUPER_USER, PMConstants.USER_ROLE.SUPER_DEV)) {
			if (refresh) {
				String wabaserver = wabaServer();
				WabaPartnerDoc partner = getPartnerWabaDoc(null);
				MapModel resp = restService.ajax(wabaserver).path("/partners/" + partner.getPartnerId() + "/channels")
						.header("Authorization",
								String.format("%s %s", partner.getAuthorization().get("token_type"),
										partner.getAuthorization().get("access_token")))
						.get().acceptJson().asMapModel();
				List<Map<String, Object>> channels = resp.keyEntry("partner_channels").asListOfMap();
				MapModel model = MapModel.createInstance();
				for (Map<String, Object> channel : channels) {
					model.fromMap(channel);
					String channelId = model.getString("id");
					String clientId = model.getString("client_id");
					if (ArgUtil.is(channelId)) {
						WabaChannelDoc channelDoc = mongoTemplate.findByIdSafeCheck(channelId, WabaChannelDoc.class);
						if (ArgUtil.not(channelDoc)) {
							channelDoc = new WabaChannelDoc();
						}
						channelDoc.setClientId(clientId);
						channelDoc.setId(channelId);
						channelDoc.setChannel(channel);
						channelDoc.setSyncdStamp(System.currentTimeMillis());
						mongoTemplate.save(channelDoc);
					}
				}
			}
			channelDocs = mongoTemplate.findAll(WabaChannelDoc.class);
		} else {
			channelDocs = mongoTemplate
					.find(MQB.collection(WabaChannelDoc.class).where(Criteria.where("_id").in(allowedChannels)));
		}
		return ApiResponse.buildResults(channelDocs);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/waba/clients/channels/{channel_id}/api_keys" }, method = RequestMethod.GET)
	public ApiResponse<Object, Object> generateKey(@PathVariable(name = "channel_id") String channelId)
			throws NoSuchAlgorithmException {

		BusinessUserDoc currentUser = userSessionBean.domainUser();
		if (!ArgUtil.is(currentUser)) {
			ApiResponseUtil.throwException("Access Denied");
		}

		BusinessUserDoc domainUser = currentUser;
		Collection<String> allowedChannels = domainUser.wabaChannels();

		if (userSessionBean.hasRoleAny(PMConstants.USER_ROLE.DUPER_USER, PMConstants.USER_ROLE.SUPER_DEV)
				|| allowedChannels.contains(channelId)) {
			String wabaserver = wabaServer();
			WabaPartnerDoc partner = getPartnerWabaDoc(null);
			try {
				MapModel resp = restService.ajax(wabaserver)
						.path("/partners/" + partner.getPartnerId() + "/channels/" + channelId + "/api_keys")
						.header("Authorization",
								String.format("%s %s", partner.getAuthorization().get("token_type"),
										partner.getAuthorization().get("access_token")))
						.post().acceptJson().asMapModel();
				WabaChannelDoc channelDoc = mongoTemplate.findByIdSafeCheck(channelId, WabaChannelDoc.class);
				if (ArgUtil.not(channelDoc)) {
					channelDoc = new WabaChannelDoc();
				}
				channelDoc.setId(channelId);
				channelDoc.setKey(resp.toMap());
				mongoTemplate.save(channelDoc);
				return ApiResponse.buildResult(resp.toMap());
			} catch (ApiHttpClientException e) {

			

				//e.printStackTrace();
				MapModel error = MapModel.from(e.getResponse().getBody());
				if(ArgUtil.is(error)) {

					ApiResponseUtil.addError(error.pathEntry("/meta/developer_message").asString());
				}
				throw e;
			}

		} else {
			if (!ArgUtil.is(currentUser)) {
				ApiResponseUtil.throwException("Access Denied");
			}
		}
		return ApiResponse.build();
	}
}
