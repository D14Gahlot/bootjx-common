package com.boot.jx.agent;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.DEFAULT;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.stomp.StompQuery;
import com.boot.jx.stomp.StompTunnelSessionManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class AgentSessionService
		implements LogoutHandler, ApplicationListener<SessionDestroyedEvent>, AuditDetailProvider {

	public static final Logger LOGGER = LoggerService.getLogger(AgentSessionService.class);
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private PMClientConfig chatClientConfig;

	@Autowired
	public MessageContext messageContext;

	/*
	 * Below APIs are
	 * 
	 * APIs for currently logged in user only
	 */

	@Autowired(required = false)
	private AgentSessionBean agentSessionBean;

	@Autowired
	private DocumentUpdateListner documentUpdateListner;

	@Autowired
	private AgentAuthProvider authProvider;

	@Autowired
	private StompTunnelSessionManager stompTunnelSessionManager;

	public List<AgentSessionDoc> getAgentSessions() {
		MongoQueryBuilder<AgentSessionDoc> builder = MongoQueryBuilder.collection(AgentSessionDoc.class)
				.where("isEnabled", true);
		return mongoTemplate.find(builder.getQuery(), AgentSessionDoc.class);
	}

	public void updateSession(boolean publish, AgentSessionBean agentSession) {

		MongoQueryBuilder<AgentSessionDoc> builder = MongoQueryBuilder.collection(AgentSessionDoc.class)
				.whereId(agentSession.getAgentCode());
		builder.set("agentCode", agentSession.getAgentCode());
		builder.set("agentDept", agentSession.getAgentDept());
		builder.set("isLoggedIn", agentSession.isLoggedIn());
		builder.set("isOnline", agentSession.isOnline());
		builder.set("isAway", agentSession.isAway());
		builder.set("lastOnlineStamp", agentSession.getLastOnlineStamp());
		builder.set("domain", AppContextUtil.getTenant());
		builder.set("profile", agentSession.getProfile());

		if (ArgUtil.is(agentSession.getProfile())) {
			builder.set("isEnabled", agentSession.getProfile().isEnabled());
		}
		mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), AgentSessionDoc.class);

		if (publish) {
			documentUpdateListner.onAgentSessionUpdate(agentSession.getAgentCode());
		}

		agentSession.setLastSyncStamp(System.currentTimeMillis());
	}

	/**
	 * Refreshes online status for currently logged in agent
	 */
	public void refreshOnline() {
		if (TimeUtils.isExpired(agentSessionBean.getLastSyncStamp(),
				chatClientConfig.getAgentSessionTimeout().toMillis())) {
			this.updateSession(true, agentSessionBean);
		}
	}

	public void setAway(boolean isAway) {
		agentSessionBean.setAway(isAway);
	}

	public void setOnline(boolean isOnline) {
		boolean oldOnline = agentSessionBean.isOnline();
		agentSessionBean.setOnline(isOnline);
		agentSessionBean.setLastOnlineStamp(System.currentTimeMillis());
		if (oldOnline != isOnline) {
			this.updateSession(true, agentSessionBean);
		} else {
			this.updateSession(false, agentSessionBean);
		}
	}

	public void updateLogin(AgentResponseAuthDto agent) {
		agentSessionBean.setProfile(agent);
		agentSessionBean.setLoggedIn(true);
		agentSessionBean.setAgentCode(agent.getAgent_code());
		if (ArgUtil.is(agent.getDept())) {
			agentSessionBean.setAgentDept(agent.getDept().getDept_code());
		}
		agentSessionBean.setOnline(true);
		agentSessionBean.setLastOnlineStamp(System.currentTimeMillis());
		agentSessionBean.getAgentCode();
		agentSessionBean.addRole(PMConstants.USER_ROLE.AGENT);
		this.updateSession(true, agentSessionBean);
	}

	/**
	 * Refreshes logout status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogout(AgentPrincipal agentPrincipal) {
		AgentSessionBean agentSession = ArgUtil.is(agentSessionBean) ? agentSessionBean : new AgentSessionBean();
		agentSession.setLoggedIn(false);
		agentSession.setOnline(false);
		agentSession.setAway(true);
		agentSession.setAgentCode(agentPrincipal.getAgentCode());
		// agentSessionBean.setAgentDept("ONLINE");
		this.updateSession(true, agentSession);
	}

	public void login(HttpServletRequest request, AgentResponseAuthDto agent, String passhash) {

		AgentPrincipal agentPrincipal = new AgentPrincipal();
		agentPrincipal.setAgentCode(agent.getAgent_code());
		agentPrincipal.setDomain(AppContextUtil.getTenant());
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(agentPrincipal, passhash);
		token.setDetails(new WebAuthenticationDetails(request));
		Authentication authentication = authProvider.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		stompTunnelSessionManager.registerUser(agent.getAgent_code(), agent.getDept().getDept_code(), DEFAULT.NO_DEPT,
				StompQuery.PING_TAG);
		updateLogin(agent);
	}

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (ArgUtil.is(authentication)) {
			try {
				AgentPrincipal agentPrincipal = (AgentPrincipal) authentication.getPrincipal();
				updateLogout(agentPrincipal);
			} catch (Exception e) {
				LOGGER.error("logout(Authentication)", e);
			}
		}
		commonHttpRequest.instance(request, response, appConfig).setCookie("JXSESSIONID", "JXSESSIONID", 0);
	}

	@Override
	public void onApplicationEvent(SessionDestroyedEvent event) {
		for (SecurityContext securityContext : event.getSecurityContexts()) {
			try {
				Authentication authentication = securityContext.getAuthentication();
				AgentPrincipal agentPrincipal = (AgentPrincipal) authentication.getPrincipal();
				AppContextUtil.clear();
				AppContextUtil.setTenant(agentPrincipal.getDomain());
				AppContextUtil.init();
				updateLogout(agentPrincipal);
			} catch (Exception e) {
				LOGGER.error("onApplicationEvent(SessionDestroyedEvent)", e);
			}
		}
	}

	public static class AgentPrincipal implements Serializable, Principal {
		private static final long serialVersionUID = 1L;
		String domain;
		String agentCode;

		public String getDomain() {
			return domain;
		}

		public void setDomain(String domain) {
			this.domain = domain;
		}

		public String getAgentCode() {
			return agentCode;
		}

		public void setAgentCode(String agentCode) {
			this.agentCode = agentCode;
		}

		@Override
		public String getName() {
			return this.agentCode;
		}
	}

	@Override
	public String getAuditUser() {
		if (RequestContextHolder.getRequestAttributes() != null) {
			if (ArgUtil.is(getAuthUser())) {
				return getAuthUser().getAuthUser();
			}
		}
		String user = messageContext.getActiveQueueCode();
		if (ArgUtil.is(user)) {
			return user;
		}
		return ArgUtil.anyOf(chatClientConfig.getDefaultSender(), PMConstants.DEFAULT.NO_USER);
	}

	@Override
	public AppAuthUser getAuthUser() {
		return this.agentSessionBean;
	}

}
