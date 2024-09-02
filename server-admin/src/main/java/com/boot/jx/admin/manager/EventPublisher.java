package com.boot.jx.admin.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.common.doc.JobScheduledDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class EventPublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisher.class);

	@Autowired
	PMEnvironment pmEnvironment;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private TunnelService tunnelService;

	public void publishEvent(String bulkSessid, String template) throws Exception {
		boolean booTempButtons = false;
		if (!StringUtils.isBlank(template)) {
			booTempButtons = checkTemplateButton(template);
		}
		if (booTempButtons) {
			String tnt = AppContextUtil.getTenant();
			String jobSchId = creteJobschedule(bulkSessid, tnt);
			if(ArgUtil.is(jobSchId)) {
				Map<String, Object> eventPayload = creteJsonMap(jobSchId, tnt);
				/** publish an event **/
				tunnelService.task(eventPayload.get("topic").toString(), eventPayload);
				//Listen event pending 
			}

		}
	}

	private boolean checkTemplateButton(String template) {
		boolean foundButton = false;
		// TODO Auto-generated method stub
		HSMTemplateDoc templateDoc = commonMongoTemplate.findById(template, HSMTemplateDoc.class);
		if (templateDoc != null) {
			String jsonString = JsonUtil.toJson(templateDoc);
			// Convert the string to a JSONObject
			JSONObject jsonObject = new JSONObject(jsonString);
			foundButton = hasButtons(jsonObject);
		}
		return foundButton;
	}

	public Map<String, Object> creteJsonMap(String jobId, String tnt) {
		try {
			// Create a Map to hold the JSON structure
			Map<String, Object> event = new HashMap<>();

			// Add the "topic" field
			event.put("topic", "CAMPAIGN_SUMMARY");

			// Create a nested map for "data"
			Map<String, String> dataMap = new HashMap<>();
			dataMap.put("jobId", jobId);
			event.put("data", dataMap);

			// Create a nested map for "context"
			Map<String, String> contextMap = new HashMap<>();
			contextMap.put("tenant", tnt);
			event.put("context", contextMap);

			// Use Jackson's ObjectMapper to convert the map to a JSON string
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(event);

			// Output the JSON string
			System.out.println(jsonString);
			return event;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String creteJobschedule(String bulkSeeId, String tnt) {
		try {

			JobScheduledDoc doc = new JobScheduledDoc();
			Map<String, List<Object>> input = new HashMap<>();
			List<Object> lstObj = new ArrayList<>();

			doc.setInput(input);
			doc.setIsactive(Constants.YES);
			doc.setJobtype("CAMPAIGN_SUMMARY");
			doc.setTime(TimeStampIndex.now());
			doc.setStatus(ArgUtil.parseAsString(Status.CRTD));

			Map<String, Object> jsonMap = new HashMap<>();

			// Step 3: Create a nested map for the "input" field
			Map<String, String> inputMap = new HashMap<>();
			inputMap.put("bulkSessionId", bulkSeeId);
			// Add the nested map to the main map
			jsonMap.put("input", inputMap);

			// Create a nested map for "context"
			Map<String, String> contextMap = new HashMap<>();
			contextMap.put("tenant", tnt);
			jsonMap.put("context", contextMap);

			lstObj.add(jsonMap);

			input.put("files", lstObj);
			doc.setInput(input);

			commonMongoTemplate.save(doc);

			return doc.getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean hasButtons(JSONObject jsonObj) {
		// Check if "options" exists and is a JSONObject
		if (jsonObj.has("options") && jsonObj.get("options") instanceof JSONObject) {
			JSONObject options = jsonObj.getJSONObject("options");
			// Check if "buttons" exists and is a JSONArray
			if (options.has("buttons")) {
				JSONArray buttons = options.optJSONArray("buttons");
				// If buttons is null or empty, return false
				return buttons != null && buttons.length() > 0;
			}

			// Check if "waba" exists and is a JSONObject
			if (options.has("waba") && options.get("waba") instanceof JSONObject) {
				JSONObject waba = options.getJSONObject("waba");

				// Check if "components" exists and is a JSONArray
				if (waba.has("components") && waba.get("components") instanceof JSONArray) {
					JSONArray components = waba.getJSONArray("components");
					// Use stream API to check if any component has non-empty buttons
//					return components.toList().stream().filter(obj -> obj instanceof JSONObject)
//							.map(obj -> (JSONObject) obj).map(component -> component.optJSONArray("buttons"))
//							.filter(buttons -> buttons != null && buttons.length() > 0).findFirst().isPresent();

					// Iterate through each component to check for buttons
					
					 for (int i = 0; i < components.length(); i++) { 
						 JSONObject component =components.getJSONObject(i); // Check if "buttons" exists and is a JSONArray
					  if (component.has("buttons")) { 
						  JSONArray buttons = component.optJSONArray("buttons"); // If buttons is null or empty, 
						  return buttons != null && buttons.length() > 0; 
					 } 
					  }
					 
				}
			}

		}

		return false;
	}

}
