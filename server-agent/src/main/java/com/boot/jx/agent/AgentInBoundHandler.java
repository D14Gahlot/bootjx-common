package com.boot.jx.agent;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.common.config.DefaultChatBoundHandler;
import com.boot.jx.common.config.CONFIG_SETUP_KEY;
import com.boot.jx.common.service.SessionEventTimer;
import com.boot.jx.inbound.InBound.SessionAssginHandler;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.CommonServiceClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapEntry;
import com.boot.model.MapModel.NodeEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class AgentInBoundHandler extends DefaultChatBoundHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentInBoundHandler.class);

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private SessionEventTimer sessionEventTimer;

	@Autowired
	private AgentChatHandler agentChatHandler;

	@Autowired(required = false)
	private ChatService chatService;

	// @Autowired
	protected SessionAssginHandler sessionAssginHandler;

	@Autowired
	private SessionStore sessionStore;
	
	@Autowired
	private CommonServiceClient commonServiceClient;

	/**
	 * EVENTS
	 */
	@Override
	public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
		ClientApp defaultClient = context().clientApp(inboxMessage.session().getQueue(), inboxMessage.contact());

		if (ArgUtil.isEmpty(inboxMessage.session().getMode()) && ArgUtil.isEmpty(inboxMessage.session().getQueue())) {
			if (!ArgUtil.is(session)) {
				session = sessionStore.getSession(inboxMessage.getSessionId());
			}
			// InBoundEvent assignEvent = assignSessionToAgent(session, null, null).value();
		}
		agentChatHandler.onMessageReceive(inboxMessage);
	}

	private MapEntry getTemplate(MapModel props, String propKey, CONFIG_SETUP_KEY KEY) {
		MapEntry talk2agent = props.keyEntry(propKey);
		if (talk2agent.exists()) {
			return talk2agent;
		}
		return pmEnvironment.keyEntry(KEY);
	}

	private void onAssign(ChatSessionDoc session, InBoundEvent assignEvent) {
		OutboxMessage oMsg = new OutboxMessage();
		try {
			ClientApp app = this.context().clientApp();

			if (ArgUtil.not(app)) {
				logManager.addTrace(assignEvent, "NoQueueFound", session.contact());
				app = this.context().clientApp(PMConstants.DEFAULT.AGENT_QUEUE_CODE, session.contact());
			}

			MapModel props = MapModel.from(app.props());

			oMsg.route().setQueueCode(app.getQueue());
			oMsg.route().setSendMode(app.getAppMode());
			oMsg.route().setSenderApp(app.getAppType());
			oMsg.route().setSenderType(MESSAGE_SENDER_TYPE.SYSTEM);

			if (!ArgUtil.is(assignEvent.sessionAssigned().oldAgent)
					&& ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {

				MapEntry templ = getTemplate(props, "agent_connected",
						CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_TALK2AGENT);
				if (templ.exists()) {
					agentChatHandler.doReply(session, oMsg.template(templ.asString()));
					return;
				}
			} else if (!ArgUtil.is(assignEvent.sessionAssigned().oldAgent)
					&& !ArgUtil.is(assignEvent.sessionAssigned().newAgent)) {
				PMConfigurationObject schedule1 = pmEnvironment
						.keyEntry(CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_SCHEDULE);
				String schedule=schedule1.asString();
				
				Map apiResponse =(Map) commonServiceClient.getScheduleStatus(schedule);
		        boolean isWorkingDay = false;

		        try {
		            if (((java.util.Map<String, Object>) apiResponse).containsKey("results")) {
		                ObjectMapper objectMapper = new ObjectMapper();
		                JsonNode resultsNode = objectMapper.convertValue(((java.util.Map<String, Object>)apiResponse).get("results"), JsonNode.class);

		                if (resultsNode.isArray()) {
		                    for (JsonNode result : resultsNode) {
		                        JsonNode flagsNode = result.path("flags");

		                        if (flagsNode.isObject()) {

		                            if (flagsNode.has("isWorkingDayToday") && flagsNode.get("isWorkingDayToday").asBoolean()) {
		                                isWorkingDay = true;
		                                		                            } 

		                        
		                        }
		                    }
		                } else {
		                    LOGGER.warn("No result is found.");
		                }
		            } else if (((java.util.Map<String, Object>) apiResponse).containsKey("error")) {
		            } else {
		            }
		        } catch (Exception e) {
		            LOGGER.error("Error recieved", e.getMessage(), e);
		        }

		        // Output based on working hours
		        if (isWorkingDay) {
		        	MapEntry templ = getTemplate(props, "agent_notfound",
    						CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_NOAGENT);
    				if (templ.exists()) {
    					agentChatHandler.doReply(session, oMsg.template(templ.asString()));
    					return;

		        } 
    				
		        }else {
		        	MapEntry templ = getTemplate(props, "agent_orgoffline",
    						CONFIG_SETUP_KEY.POSTMAN_AGENT_CHAT_AUTOREPLY_ORGOFFLINE);
    				if (templ.exists()) {
    					agentChatHandler.doReply(session, oMsg.template(templ.asString()));
    					return;
    				}

		        }

		    }
				
				
				
			
			else {

				MapEntry templ = props.keyEntry("agent_transfer");
				if (templ.exists()) {
					agentChatHandler.doReply(session, oMsg.template(templ.asString()));
					return;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error ONE while Connecting to Agent", e);
			logManager.error(assignEvent, e);
			try {
				agentChatHandler.doReply(session, oMsg.message(
						"We are having some issues trying connect you to one of our customer representatives. Please be patient"));
			} catch (InterruptedException e1) {
				LOGGER.error("Error TWO  while Sending Failure", e1);
				logManager.error(assignEvent, e1);
			}
		}
	}
				                    

	/**
	 * METHODS/ACTION
	 */
	@Override
	public NodeEntry<InBoundEvent> assignSessionToAgent(PMArgs params, ChatSessionDoc session) {
		NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();
		InBoundEvent agentAssignEvent = new InBoundEvent();
		agentAssignEvent.eventCode = InBoundEvent.SESSION_ASSIGNED;
		agentAssignEvent.sessionId = session.getSessionId();
		agentAssignEvent.sessionAssigned().oldAgent = session.getAssignedToAgent();
		agentAssignEvent.sessionAssigned().oldDept = session.getAssignedToDept();
		params = agentChatHandler.doAssign(session, params);
		if (ArgUtil.is(params)) {
			agentAssignEvent.sessionAssigned().newDept = params.getAssignToDeptCode();
			agentAssignEvent.sessionAssigned().newAgent = params.getAssignToAgentCode();
		}
		onAssign(session, agentAssignEvent);
		return eventEntry.value(agentAssignEvent);
	}

	@Override
	public CHAT_MODE mode() {
		return CHAT_MODE.AGENT;
	}

	@Override
	public void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
		ClientApp targetAppQueue = context().clientApp(inBoundEvent.sessionRouted.targetQueue, null);
		MapModel props = new MapModel(targetAppQueue.props());
		AppContextUtil.setActorId(targetAppQueue.getQueue());
		assignSessionToAgent(new PMArgs().contact(pmArgs.contact())
				.assignToDeptCode(ArgUtil.nonEmpty(pmArgs.getAssignToDeptCode(), props.getString("deptCode")))
				.assignToAgentCode(ArgUtil.nonEmpty(pmArgs.getAssignToAgentCode(), props.getString("agentCode")))
				.assignToSkillCodes(pmArgs.getAssignToSkillCodes()), sessionDoc);
		sessionEventTimer.setMitelRoutingCheck(sessionDoc.getSessionId(), targetAppQueue);
	}

}
