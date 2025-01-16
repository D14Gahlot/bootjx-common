package com.boot.jx.postman.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.channel.ChannelClientFactory;
import com.boot.jx.postman.channel.ChannelClientFactory.ChannelClient;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.tpo.WABAFlows;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360Template;
import com.boot.jx.postman.wacfb.WacfbClient;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ThirdPartyTemplateManager {

	@Autowired
	private WA360Client wa360Client;

	@Autowired
	private ChannelClientFactory clientFactory;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public void refreshWA360Templates(ChannelConfig channelConfig) {
		MapModel resp = null;

		ChannelClient channelClient = clientFactory.get(channelConfig);

		resp = channelClient.fetchTemplates(channelConfig);
		if (resp.containsKey("data")) {
			Object value = resp.get("data");
			resp.remove("data");
			resp.put("waba_templates", value);
		}
		List<WA360Template> wabaTemplates = resp.keyEntry("waba_templates").asList(WA360Template.class);// null
		MongoQueryBuilder<HSMTemplate3rdParty> cmqb = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(channelConfig.getChannelId())).set("template.status", "deleted");

		commonMongoTemplate.update(cmqb);

		for (WA360Template wa360Template : wabaTemplates) {
			HSMTemplate3rdParty thirdPartyTemplate = toHSM3rdParty(channelConfig, wa360Template);
			commonMongoTemplate.save(thirdPartyTemplate);
			linkRefresh(thirdPartyTemplate, thirdPartyTemplate.getHsmTemplateId(),
					ArgUtil.parseAsString(thirdPartyTemplate.getTemplate().get("status"), Constants.BLANK));
		}
	}

	private HSMTemplate3rdParty toHSM3rdParty(ChannelConfig channelConfig, WA360Template wa360Template) {
		String id = String.format("%s/%s/%s", channelConfig.getChannelId(), wa360Template.getName(),
				wa360Template.getLanguage());
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(id, HSMTemplate3rdParty.class);// coming
																												// null
		if (!ArgUtil.is(thirdPartyTemplate)) {
			thirdPartyTemplate = new HSMTemplate3rdParty();
			thirdPartyTemplate.setId(id);
		}

		thirdPartyTemplate.setChannelId(channelConfig.getChannelId());
		thirdPartyTemplate.setCode(wa360Template.getName());
		thirdPartyTemplate.setLang(wa360Template.getLanguage());

		// thirdPartyTemplate.setCategory(wa360Template.getCategory());
		thirdPartyTemplate.setContactType(ArgUtil.parseAsString(channelConfig.getContactType()));
		thirdPartyTemplate.setChannelType(channelConfig.getChannelType());

		thirdPartyTemplate.setTemplate(JsonUtil.toMap(wa360Template));
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty createhWA360Templates(ChannelConfig channelConfig,
			Map<String, Object> templateStructure) {
		String status = ArgUtil.parseAsString(templateStructure.get("status"), Constants.BLANK);
		MapModel resp = null;

		ChannelClient channelClient = clientFactory.get(channelConfig);
		if ("approved".equalsIgnoreCase(status) || "rejected".equalsIgnoreCase(status)
				|| "paused".equalsIgnoreCase(status)) {
			resp = channelClient.updateTemplates(channelConfig, MapModel.from(templateStructure));
		} else {
			resp = channelClient.createTemplates(channelConfig, MapModel.from(templateStructure));
			resp.put("language", templateStructure.get("language"));
			resp.put("name", templateStructure.get("name"));
			resp.put("status", templateStructure.get("status"));
			resp.put("rejected_reason", templateStructure.get("rejected_reason"));

		}
		return toHSM3rdParty(channelConfig, resp.as(WA360Template.class));
	}

	public HSMTemplate3rdParty deleteWA360Templates(ChannelConfig channelConfig, HSMTemplate3rdParty temp) {
		WA360Template x = JsonUtil.toObject(temp.getTemplate(), WA360Template.class);
		if (ArgUtil.is(x)) {
			if (!"deleted".equalsIgnoreCase(x.getStatus())) {
				wa360Client.deleteTemplates(channelConfig, x.getName());
			}
			commonMongoTemplate.remove(temp);
		}
		return temp;
	}

	public HSMTemplate3rdParty migrateWABATemplate(HSMTemplate3rdParty fromTemplate, String toChannelId) {
		Contactable channelInfo = PostManUtil.parseChannelId(toChannelId);
		HSMTemplate3rdParty newTemp = JsonUtil.deepCopy(fromTemplate, HSMTemplate3rdParty.class);
		newTemp.setChannelId(toChannelId);
		newTemp.setChannelType(channelInfo.getChannelType());
		newTemp.setContactType(channelInfo.getContactType());
		return newTemp;
	}

	public List<HSMTemplate3rdParty> migrateWABATemplate(String fromChannelId, String toChannelId) {
		MongoQueryBuilder<HSMTemplate3rdParty> q = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(fromChannelId));
		List<HSMTemplate3rdParty> tmps = commonMongoTemplate.find(q);
		for (HSMTemplate3rdParty hsmTemplate3rdParty : tmps) {
			HSMTemplate3rdParty newTemp = migrateWABATemplate(hsmTemplate3rdParty, toChannelId);
			commonMongoTemplate.save(newTemp);
		}
		MongoQueryBuilder<HSMTemplate3rdParty> q2 = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(toChannelId));
	
		return commonMongoTemplate.find(q2);
	}
    /*
	public HSMTemplate3rdParty migrateWABATemplate(HSMTemplate3rdParty fromTemplate, String toChannelId) {
	    Contactable channelInfo = PostManUtil.parseChannelId(toChannelId);
	    HSMTemplate3rdParty newTemp = JsonUtil.deepCopy(fromTemplate, HSMTemplate3rdParty.class);
	    newTemp.setChannelId(toChannelId);
	    newTemp.setChannelType(channelInfo.getChannelType());
	    newTemp.setContactType(channelInfo.getContactType());

	    if (fromTemplate.getVarMap() != null) {
	        newTemp.setVarMap(new HashMap<>(fromTemplate.getVarMap()));
	    } else {
	        newTemp.setVarMap(null);
	    }

	    return newTemp;
	}

	public List<HSMTemplate3rdParty> migrateWABATemplate(String fromChannelId, String toChannelId) {
	    MongoQueryBuilder<HSMTemplate3rdParty> fromQuery = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
	            .where(Criteria.where("channelId").is(fromChannelId));
	    List<HSMTemplate3rdParty> fromTemplates = commonMongoTemplate.find(fromQuery);

	    MongoQueryBuilder<HSMTemplate3rdParty> toQuery = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
	            .where(Criteria.where("channelId").is(toChannelId));
	    List<HSMTemplate3rdParty> toTemplates = commonMongoTemplate.find(toQuery);

	    List<String> toTemplateIds = new ArrayList<>();
	    for (HSMTemplate3rdParty template : toTemplates) {
	        toTemplateIds.add(template.getTemplate() + "_" + template.getLang());
	    }

        for (HSMTemplate3rdParty fromTemplate : fromTemplates) {
	        String fromTemplateIds = fromTemplate.getTemplate() + "_" + fromTemplate.getLang();

	        if (!toTemplateIds.contains(fromTemplateIds)) {
	            HSMTemplate3rdParty newTemp = migrateWABATemplate(fromTemplate, toChannelId);
	            commonMongoTemplate.save(newTemp);
	        }
	    }
//if template is there not varmap- copy varmap
	    
	    return commonMongoTemplate.find(toQuery);
	}
*/
	public List<HSMTemplate3rdParty> getTemplates(ChannelConfig channelConfig, String code) {
		MongoQueryBuilder<HSMTemplate3rdParty> q = MongoQueryBuilder.collection(HSMTemplate3rdParty.class)
				.where(Criteria.where("channelId").is(channelConfig.getChannelId()));

		if (ArgUtil.is(code)) {
			q.where("code", code);
		}

		return commonMongoTemplate.find(q);
	}

	public List<HSMTemplate3rdParty> getTemplates(ChannelConfig channelConfig) {
		return this.getTemplates(channelConfig, null);
	}
	public List<WABAFlows> getFlows(ChannelConfig channelConfig, String code) {
		
		MongoQueryBuilder<WABAFlows> q = MongoQueryBuilder.collection(WABAFlows.class)
				.where(Criteria.where("wabaId").is(channelConfig.getWacfb().getWabaId()));

		if (ArgUtil.is(code)) {
			q.where("code", code);
		}

		return commonMongoTemplate.find(q);
	}
	

	public List<WABAFlows> getFlows(ChannelConfig channelConfig) {
		return this.getFlows(channelConfig, null);
	}
	public HSMTemplate3rdParty link(String thirdPartyTemplateId, String hsmTemplateId) {
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(thirdPartyTemplateId,
				HSMTemplate3rdParty.class);
		String hsmTemplateIdOld = thirdPartyTemplate.getHsmTemplateId();
		thirdPartyTemplate.setHsmTemplateId(hsmTemplateId);

		commonMongoTemplate.save(thirdPartyTemplate);

		this.linkRefresh(thirdPartyTemplate, hsmTemplateIdOld, null);
		this.linkRefresh(thirdPartyTemplate, hsmTemplateId,
				ArgUtil.parseAsString(thirdPartyTemplate.getTemplate().get("status"), Constants.BLANK));
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty linkRefresh(HSMTemplate3rdParty thirdPartyTemplate, String hsmTemplateId,
			String status) {
		if (ArgUtil.is(hsmTemplateId)) {

			HSMTemplateDoc hsmTemplateDoc = commonMongoTemplate.findById(hsmTemplateId, HSMTemplateDoc.class);
			if (ArgUtil.is(hsmTemplateDoc)) {
				hsmTemplateDoc.options().put("waba", thirdPartyTemplate.getTemplate());

				hsmTemplateDoc.approved(thirdPartyTemplate.getChannelId(), thirdPartyTemplate.getHsmTemplateId(),
						status);
				commonMongoTemplate.save(hsmTemplateDoc);
			}
		}
		return thirdPartyTemplate;
	}

	public HSMTemplate3rdParty varMap(String thirdPartyTemplateId, Map<String, Object> varMap) {
		HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(thirdPartyTemplateId,
				HSMTemplate3rdParty.class);
		thirdPartyTemplate.setVarMap(varMap);
		commonMongoTemplate.save(thirdPartyTemplate);
		return thirdPartyTemplate;
	}

	public void refreshWabaFlows(ChannelConfig channelConfig) {
		ChannelClient channelClient = clientFactory.get(channelConfig);
		MapModel resp = channelClient.listOfFlows(channelConfig);
		List<Map<String, Object>> flows = resp.entry("data").asListOfMap();

		for (Map<String, Object> flowData : flows) {
			String flowId = (String) flowData.get("id");
			
			String id = String.format("%s/%s", channelConfig.getWacfb().getWabaId(), flowId);
			WABAFlows flowDoc = commonMongoTemplate.findById(id, WABAFlows.class);
			if (!ArgUtil.is(flowDoc)) {
				flowDoc = new WABAFlows();
			}
			flowDoc.setWabaId(channelConfig.getWacfb().getWabaId());
			flowDoc.setId(id);
			flowDoc.setFlowId(id);
			flowDoc.setMeta(flowData);
			commonMongoTemplate.save(flowDoc);
			fetchAndSetScreenId(flowId, channelConfig);

		}

	}

	@Autowired
	WacfbClient wacfbClient;

	@Async
	public void fetchAndSetScreenId(String flowId, ChannelConfig channelConfig) {

		try {
			ChannelClient channelClient = clientFactory.get(channelConfig);
			MapModel resp = channelClient.flowsAssets(flowId, channelConfig);

			List<Map<String, Object>> data = (List<Map<String, Object>>) resp.toMap().get("data");

			if (data != null && !data.isEmpty()) {
				String downloadUrl = (String) data.get(0).get("download_url");

				RestTemplate restTemplate = new RestTemplate();
				String jsonResponseString = restTemplate.getForObject(downloadUrl, String.class);

				ObjectMapper objectMapper = new ObjectMapper();

				Map<String, Object> jsonResponse = objectMapper.readValue(jsonResponseString,
						new TypeReference<Map<String, Object>>() {
						});
		        Map<String, Object> lastOnClickAction = fetchLastOnClickAction(jsonResponse);

				List<Map<String, String>> fieldMe = fetchFieldMeta(jsonResponse);


				String id = String.format("%s/%s", channelConfig.getWacfb().getWabaId(), flowId);
				WABAFlows flow = commonMongoTemplate.findById(id, WABAFlows.class);

				if (flow != null) {
					if (jsonResponse != null) {
						flow.setJson(jsonResponse);
						flow.setFieldMeta(fieldMe);
					}

					commonMongoTemplate.save(flow);
				}
			}

		} catch (Exception e) {
			System.out.print(e);
		}
	}

	  public static Map<String, Object> fetchLastOnClickAction(Map<String, Object> jsonResponse) {
	        Map<String, Object> lastPayload = null;

	        // Navigate to the layout field
	        
	        List<Map<String, Object>> screens = (List<Map<String, Object>>) jsonResponse.get("screens");
	        if (screens != null && !screens.isEmpty()) {
	            // Get the last screen
	            Map<String, Object> lastScreen = screens.get(screens.size() - 1);

	            // Navigate to the layout field
	            Map<String, Object> layout = (Map<String, Object>) lastScreen.get("layout");
	            if (layout != null) {
	                List<Map<String, Object>> children = (List<Map<String, Object>>) layout.get("children");
	                if (children != null) {
	                    // Iterate through the children to find the Footer type
	                    for (Map<String, Object> child : children) {
	                        if ("Footer".equals(child.get("type"))) {
	                            // Extract the on-click-action field
	                            Map<String, Object> onClickAction = (Map<String, Object>) child.get("on-click-action");
	                            if (onClickAction != null) {
	                                // Get the payload field from the on-click-action
	                                lastPayload = (Map<String, Object>) onClickAction.get("payload");
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        return lastPayload;
	    }
	
	public static List<Map<String, String>> fetchFieldMeta(Map<String, Object> jsonResponse) throws IOException {
		List<Map<String, String>> fieldMeta = new ArrayList<>();
		List<Map<String, Object>> screens = (List<Map<String, Object>>) jsonResponse.get("screens");

		if (screens != null) {
			for (int i = 0; i < screens.size(); i++) {
				Map<String, Object> screen = screens.get(i);
				Map<String, Object> layout = (Map<String, Object>) screen.get("layout");
				if (layout != null) {
					fetchChildren((List<Map<String, Object>>) layout.get("children"), fieldMeta, i);
				}
			}
		}

		return fieldMeta;
	}

	private static void fetchChildren(List<Map<String, Object>> children, List<Map<String, String>> fieldMeta,
			int screenIndex) {
		if (children != null) {
			int inputIndex = 0;
			for (Map<String, Object> child : children) {
				String type = (String) child.get("type");

				if ("Form".equals(type)) {
					Object childChildren = child.get("children");
					if (childChildren instanceof List) {
						fetchChildren((List<Map<String, Object>>) childChildren, fieldMeta, screenIndex);
					}
				} else if (isFieldType(type)) {
					String label = (String) child.get("label");
					label = label.replace(" ", "_");
					String key = "screen_" + screenIndex + "_" + type + "_" + inputIndex;
					String key2 = "screen_" + screenIndex + "_" + label + "_" + inputIndex;

					Map<String, String> meta = new HashMap<>();
					meta.put("key", key);
					meta.put("key2", key2);
					meta.put("label", label);
					meta.put("type", type);
					if (child.containsKey("data-source")) {
						List<Map<String, String>> dataSource = (List<Map<String, String>>) child.get("data-source");
						meta.put("data-source", dataSource.toString());
					}

					fieldMeta.add(meta);
					inputIndex++;
				}
			}
		}
	}

	private static boolean isFieldType(String type) {
		return "TextInput".equals(type) || "RadioButtonsGroup".equals(type) || "DatePicker".equals(type)
				|| "Dropdown".equals(type) || "CheckBoxGroup".equalsIgnoreCase(type) || "OptIn".equalsIgnoreCase(type)
				|| "textArea".equalsIgnoreCase(type);
	}

}
