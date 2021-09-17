package com.boot.jx.agent;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import com.boot.jx.common.doc.AgentSessionDoc;
import com.boot.jx.common.dto.AgentResponseAuthDto;
import com.boot.jx.common.store.DocumentUpdateListner;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMClientConfig;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class AgentSessionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private PMClientConfig chatClientConfig;

    /*
     * Below APIs are
     * 
     * APIs for currently logged in user only
     */

    @Autowired
    private AgentSessionBean agentSessionBean;

    @Autowired
    private DocumentUpdateListner documentUpdateListner;

    @Autowired
    private AgentAuthProvider authProvider;

    public List<AgentSessionDoc> getAgentSessions() {
	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().where("isEnabled", true);
	return mongoTemplate.find(builder.getQuery(), AgentSessionDoc.class);
    }

    public void updateSession(boolean publish) {
	CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder().whereId(agentSessionBean.getAgentCode());
	builder.set("agentCode", agentSessionBean.getAgentCode());
	builder.set("agentDept", agentSessionBean.getAgentDept());
	builder.set("isLoggedIn", agentSessionBean.isLoggedIn());
	builder.set("isOnline", agentSessionBean.isOnline());
	builder.set("lastOnlineStamp", agentSessionBean.getLastOnlineStamp());

	if (ArgUtil.is(agentSessionBean.getProfile())) {
	    builder.set("isEnabled", agentSessionBean.getProfile().isEnabled());
	}
	mongoTemplate.upsert(builder.getQuery(), builder.getUpdate(), AgentSessionDoc.class);

	if (publish) {
	    documentUpdateListner.onAgentSessionUpdate(agentSessionBean.getAgentCode());
	}

	agentSessionBean.setLastSyncStamp(System.currentTimeMillis());
    }

    /**
     * Refreshes online status for currently logged in agent
     */
    public void refreshOnline() {
	if (TimeUtils.isExpired(agentSessionBean.getLastSyncStamp(), chatClientConfig.getAgentSessionTimeout())) {
	    this.updateSession(true);
	}
    }

    public void setOnline(boolean isOnline) {
	boolean oldOnline = agentSessionBean.isOnline();
	agentSessionBean.setOnline(isOnline);
	agentSessionBean.setLastOnlineStamp(System.currentTimeMillis());
	if (oldOnline != isOnline) {
	    this.updateSession(true);
	} else {
	    this.updateSession(false);
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

	this.updateSession(true);
    }

    /**
     * Refreshes logout status for currently logged in agent
     * 
     * @param username
     */
    public void updateLogout(String username) {
	agentSessionBean.setLoggedIn(false);
	agentSessionBean.setOnline(false);
	agentSessionBean.setAgentCode(username);
	agentSessionBean.setAgentDept("ONLINE");
	this.updateSession(true);
    }

    public void login(HttpServletRequest request, AgentResponseAuthDto agent, String passhash) {
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(agent.getAgent_code(),
		passhash);
	token.setDetails(new WebAuthenticationDetails(request));
	Authentication authentication = authProvider.authenticate(token);
	SecurityContextHolder.getContext().setAuthentication(authentication);
	updateLogin(agent);
    }

}
