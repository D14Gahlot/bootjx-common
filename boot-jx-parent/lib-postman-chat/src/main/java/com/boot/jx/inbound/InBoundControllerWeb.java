package com.boot.jx.inbound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.connectors.WebConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.Kooky;
import com.boot.jx.http.RequestType;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PMAuditEvent;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.jx.stomp.StompTunnelSessionManager;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.boot.utils.UniqueID;

@Controller
public class InBoundControllerWeb {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundControllerWeb.class);
	private static final String WEB_SESSION_ID = "web-session-id";

	@Autowired
	private InBoundService inBoundService;

	@Autowired(required = false)
	private WebConnector dummyConnector;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private MessageContext messageContext;

	@Autowired
	private WebConnector connector;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired(required = false)
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private AuditService auditService;

	@Autowired
	private ChatStatusService inBoundStatusService;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	@Value("${app.stomp}")
	boolean stompEnabled;

	@ApiRequest(session = true)
	@RequestMapping(value = "/plugin/customer/**", method = RequestMethod.GET)
	public String pluginCustomer(Model model, @RequestParam(required = false) String contacyType,
			@RequestParam(required = false, defaultValue = "/plugin/customer") String path, HttpServletRequest request)
			throws InterruptedException {
		commonHttpRequest.setCookie("contactType", ArgUtil.parseAsString(contacyType, ContactType.WEBSITE.toString()));
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("WEBAPP_BASE", appConfig.getAppPrefix() + path);
		model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR",
				pmEnvironment.keyEntry("postman.agent.scheme.color").asString());

		if (pmCommonConfig != null) {
			model.addAllAttributes(pmCommonConfig.appAttributes());
		}

		String nounce = UniqueID.generateString62();
		model.addAttribute("NOUNCE", nounce);
		commonHttpRequest.setCookie("NOUNCE", nounce);
		model.addAttribute("STOMP_ENABLED", stompEnabled);
		return "app-customer";
	}

	@ApiRequest(session = true)
	@RequestMapping(value = "/ext/plugin/customer/**", method = RequestMethod.GET)
	public String pluginCustomerPub(Model model, @RequestParam(required = false) String contacyType,
			HttpServletRequest request) throws InterruptedException {
		return pluginCustomer(model, contacyType, "/ext/plugin/customer", request);
	}

	@ApiRequest(type = RequestType.POLL, session = true)
	@ResponseBody
	@RequestMapping(value = { "/ext/outbound/web/callback", "/ext/plugin/outbound/web/callback" },
			method = RequestMethod.GET)
	public OutboxMessage onReceiveMessage(@RequestParam(required = false) String number,
			@RequestParam(required = false) String csid, @RequestParam(required = false) String channelId,
			@RequestParam(required = false) String channelKey) throws InterruptedException {
		ChannelConfig channelConfig = pmEnvironment.config().channel(channelId);
		return dummyConnector
				.pollUnreadMessage(AppContextUtil.getTenant() + "/" + PostManUtil.CONTACT_ID(channelConfig, csid));
	}

	@ApiRequest(session = true)
	@RequestMapping(value = "/ext/plugin/mobile/auth", method = RequestMethod.GET)
	public String directAuth(@RequestParam(required = false) String csid,
			@RequestParam(required = false) String deviceId, @RequestParam(required = false) String channelId,
			@RequestParam(required = false) String channelKey) {
		PMConfiguration config = pmEnvironment.config();
		ChannelConfig channelConfig = config.channel(channelId);
		if (!ArgUtil.is(channelConfig) || !ArgUtil.areEqual(channelConfig.getChannelKey(), channelKey)) {
			ApiResponseUtil.throwAccessDeniedException("Invalid Channel");
		}
		// need to cpmplte for mobile auth
		return "app-customer";
	}

	@ApiRequest(session = true)
	@ResponseBody
	@RequestMapping(value = "/ext/plugin/outbound/web/auth/v2", method = RequestMethod.GET)
	public ApiResponse<ChatMessageDTO, Object> onAuthV2(@RequestParam(required = false) String user,
			@RequestParam(required = false) String number, @RequestParam(required = false) String csid,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String channelKey)
			throws InterruptedException {

		String webSessionIdKey = StringUtils.sanitize(WEB_SESSION_ID + "_" + channelId);

		String webSessionId = commonHttpRequest.get(webSessionIdKey);
		csid = ArgUtil.nonEmpty(csid, number);
		ChannelConfig channelConfig = pmEnvironment.config().channel(channelId);
		String contactId = PostManUtil.CONTACT_ID(channelConfig, csid);
		String contactIdWeb = AppContextUtil.getTenant() + "/" + contactId;

		ChatSessionDoc session = null;
		if (ArgUtil.is(webSessionId)) {
			session = sessionStore.getValidSession(webSessionId);
		}
		List<ChatMessageDTO> msgs = new ArrayList<ChatMessageDTO>();
		if (ArgUtil.is(session)) {
			List<MessageDoc> messages = messageStore.findBySessionId(webSessionId, ContactType.WEBSITE.toString());
			for (MessageDoc messageDoc : messages) {
				ChatMessageDTO outboxMessage = ChatDTOUtil.getChatMessageDTO(messageDoc);
				if (ArgUtil.isEqual(messageDoc.getType(), "I", "O")) {
					msgs.add(outboxMessage);
				}
			}
		}

		StompSession stomp = null;
		if (ArgUtil.is(channelConfig)) {
			stomp = stompTunnelSessionManager.registerUser(ArgUtil.nonEmpty(user, csid), contactIdWeb, csid);
		}

		if (msgs.size() == 0) {
			ChatContactDoc chatContactDoc = sessionStore.getContact(contactId);
			OutboxMessage icebrakerMsg = dummyConnector.onIceBreak(channelConfig, chatContactDoc);
			if (ArgUtil.is(icebrakerMsg)) {
				msgs.add(ChatDTOUtil.getChatMessageDTO(messageStore.createMessageDoc(icebrakerMsg)));
			}
		}
		//In-Cognito Window does not support Cookies that is why it So important to  send these values to UI in advance for mapping,
		//because if cookies cant be set, JSESSION cannot be created and be relied upon to store these values
		return ApiResponse.buildResults(msgs, stomp);
	}

	private ApiResponse<InboxMessage, Object> inboundMessageBoxEventMethod(String channelId, String channelKey,
			MapModel map, MultipartFile file) {
		PMConfiguration config = pmEnvironment.config();
		ChannelConfig channelConfig = config.channel(channelId);
		MapModel meta = MapModel.createInstance();
		// ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
		try {

			// if (!ArgUtil.areEqual(nounce, commonHttpRequest.get("NOUNCE"))) {
			// ApiResponseUtil.throwUnAuthorizedException("Invalid API");
			// }

			if (!ArgUtil.is(channelConfig) || !ArgUtil.areEqual(channelConfig.getChannelKey(), channelKey)) {
				ApiResponseUtil.throwAccessDeniedException("Invalid Channel");
			}

			String webSessionIdKey = StringUtils.sanitize(WEB_SESSION_ID + "_" + channelId);

			MessageBoxEvent messageBoxEvent = connector.inboundMessageBoxEvent(channelConfig, map,
					new MessageBoxEvent(), file);
			if (ArgUtil.is(messageBoxEvent.getInboxMessages())) {
				InboxMessage sessionMessage = new InboxMessage();

				messageBoxEvent.getInboxMessages().forEach(inboxMessage -> {
					inBoundService.invokeMethods(inboxMessage);
					sessionMessage.setSessionId(inboxMessage.getSessionId());
					sessionMessage.setContact(sessionMessage.getContact());
				});
				connector.onReceiveInboxMessage(messageBoxEvent.getInboxMessages());
				String webSessionId = commonHttpRequest.get(webSessionIdKey);
				if (!ArgUtil.is(webSessionId) || !webSessionId.equalsIgnoreCase(sessionMessage.getSessionId())) {
					webSessionId = sessionMessage.getSessionId();
					commonHttpRequest.setCookie(new Kooky().name(webSessionIdKey).value(webSessionId));
				}
				meta.put("webSessionIdKey", webSessionIdKey);
				meta.put("webSessionId", webSessionId);

				return ApiResponse.buildResults(messageBoxEvent.getInboxMessages(), meta.toMap());
			} else if (ArgUtil.is(messageBoxEvent.getMessageReports())) {
				connector.onMessageReports(messageBoxEvent.getMessageReports());
				inBoundStatusService.update(messageBoxEvent.getMessageReports());
			}
		} catch (Exception e) {
			auditService.excep(new PMAuditEvent(PMAuditEvent.Type.INBOUND_ERROR).data(map.toMap()), LOGGER, e);
		}

		return ApiResponse.buildResult(null, meta.toMap());
	}

	@ApiRequest(session = true)
	@ResponseBody
	@RequestMapping(value = "/ext/plugin/inbound/v2/web/callback/{nounce}/{channelId}/{channelKey}",
			method = { RequestMethod.POST })
	public ApiResponse<InboxMessage, Object> inboundMessageBoxEvent(@PathVariable(required = false) String nounce,
			@PathVariable(required = false) String channelId, @PathVariable(required = false) String channelKey,
			@RequestBody Map<String, Object> data) {
		MapModel map = MapModel.from(data);
		return inboundMessageBoxEventMethod(channelId, channelKey, map, null);
	}

	@ApiRequest(session = true)
	@ResponseBody
	@RequestMapping(value = "/ext/plugin/inbound/v2/web/callback/{nounce}/{channelId}/{channelKey}",
			method = { RequestMethod.PUT })
	public ApiResponse<InboxMessage, Object> inboundMediaBoxEvent(@PathVariable(required = false) String nounce,
			@PathVariable(required = false) String channelId, @PathVariable(required = false) String channelKey,
			@RequestParam String from, @RequestParam(name = "file") MultipartFile file) throws InterruptedException {
		MapModel data = MapModel.createInstance().put("from", from);
		return inboundMessageBoxEventMethod(channelId, channelKey, data, file);
	}

}
