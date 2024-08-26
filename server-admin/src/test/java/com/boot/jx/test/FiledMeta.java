package com.boot.jx.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.internal.filter.ValueNode;
import com.jayway.jsonpath.internal.filter.ValueNode.JsonNode;

public class FiledMeta {
	 public static Map<String, String> extractFieldMeta(String jsonString) throws IOException {
	        ObjectMapper mapper = new ObjectMapper();
	        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonString);

	        Map<String, String> fieldMeta = new LinkedHashMap<>();
	        com.fasterxml.jackson.databind.JsonNode screens = rootNode.get("screens");

	        if (screens != null && screens.isArray()) {
	            for (int i = 0; i < screens.size(); i++) {
	                com.fasterxml.jackson.databind.JsonNode screen = screens.get(i);
	                com.fasterxml.jackson.databind.JsonNode layout = screen.get("layout");
	                if (layout != null) {
	                    extractChildren(layout.get("children"), fieldMeta, i);
	                }
	            }
	        }

	        return fieldMeta;
	    }

	    private static void extractChildren(com.fasterxml.jackson.databind.JsonNode jsonNode, Map<String, String> fieldMeta, int screenIndex) {
	        if (jsonNode != null && jsonNode.isArray()) {
	            for (int i = 0, inputIndex = 0; i < jsonNode.size(); i++) {
	                com.fasterxml.jackson.databind.JsonNode child = jsonNode.get(i);
	                String type = child.get("type").asText();

	                if ("Form".equals(type)) {
	                    extractChildren(child.get("children"), fieldMeta, screenIndex);
	                } else if (isInputField(type)) {
	                    String label = child.get("label").asText();
	                    String name = child.get("name").asText();
	                    String key = "screen_" + screenIndex + "_" + type + "_" + inputIndex;
	                    fieldMeta.put(key, label);
	                    inputIndex++;
	                }
	            }
	        }
	    }

	    private static boolean isInputField(String type) {
	        return "TextInput".equals(type) || "RadioButtonsGroup".equals(type) ||
	               "DatePicker".equals(type) || "Dropdown".equals(type);
	    }

	    public static void main(String[] args) {
	        String jsonString = "{\r\n"
	        		+ "  \"version\": \"3.1\",\r\n"
	        		+ "  \"screens\": [\r\n"
	        		+ "    {\r\n"
	        		+ "      \"id\": \"QUESTION_ONE\",\r\n"
	        		+ "      \"title\": \"Al Mulla Travels - BOT\",\r\n"
	        		+ "      \"data\": {},\r\n"
	        		+ "      \"terminal\": true,\r\n"
	        		+ "      \"layout\": {\r\n"
	        		+ "        \"type\": \"SingleColumnLayout\",\r\n"
	        		+ "        \"children\": [\r\n"
	        		+ "          {\r\n"
	        		+ "            \"type\": \"Form\",\r\n"
	        		+ "            \"name\": \"flow_path\",\r\n"
	        		+ "            \"children\": [\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"TextBody\",\r\n"
	        		+ "                \"text\": \"Select 'Add content' to start building your form. To add new screens, select 'Add new' in the 'Screens' panel.\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"TextInput\",\r\n"
	        		+ "                \"label\": \"Origin\",\r\n"
	        		+ "                \"name\": \"TextInput_12a893\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"input-type\": \"text\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"TextInput\",\r\n"
	        		+ "                \"label\": \"Destination\",\r\n"
	        		+ "                \"name\": \"TextInput_b2dfc2\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"input-type\": \"text\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"RadioButtonsGroup\",\r\n"
	        		+ "                \"label\": \"Choose one\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"name\": \"RadioButtonsGroup_56d4f4\",\r\n"
	        		+ "                \"data-source\": [\r\n"
	        		+ "                  {\r\n"
	        		+ "                    \"id\": \"0_One_way\",\r\n"
	        		+ "                    \"title\": \"One way\"\r\n"
	        		+ "                  },\r\n"
	        		+ "                  {\r\n"
	        		+ "                    \"id\": \"1_Return\",\r\n"
	        		+ "                    \"title\": \"Return\"\r\n"
	        		+ "                  }\r\n"
	        		+ "                ]\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"DatePicker\",\r\n"
	        		+ "                \"label\": \"Start Date\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"name\": \"DatePicker_3b1759\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"DatePicker\",\r\n"
	        		+ "                \"label\": \"Return Date\",\r\n"
	        		+ "                \"required\": false,\r\n"
	        		+ "                \"name\": \"DatePicker_f8557a\",\r\n"
	        		+ "                \"helper-text\": \"If 'One-way' is chosen, skip this field.\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"TextInput\",\r\n"
	        		+ "                \"label\": \"Number of people\",\r\n"
	        		+ "                \"name\": \"TextInput_0fc35f\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"input-type\": \"number\"\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"Dropdown\",\r\n"
	        		+ "                \"label\": \"Type of travel\",\r\n"
	        		+ "                \"required\": true,\r\n"
	        		+ "                \"name\": \"Dropdown_4b9e34\",\r\n"
	        		+ "                \"data-source\": [\r\n"
	        		+ "                  {\r\n"
	        		+ "                    \"id\": \"0_First_\",\r\n"
	        		+ "                    \"title\": \"First \"\r\n"
	        		+ "                  },\r\n"
	        		+ "                  {\r\n"
	        		+ "                    \"id\": \"1_Business_\",\r\n"
	        		+ "                    \"title\": \"Business \"\r\n"
	        		+ "                  },\r\n"
	        		+ "                  {\r\n"
	        		+ "                    \"id\": \"2_Economy_\",\r\n"
	        		+ "                    \"title\": \"Economy \"\r\n"
	        		+ "                  }\r\n"
	        		+ "                ]\r\n"
	        		+ "              },\r\n"
	        		+ "              {\r\n"
	        		+ "                \"type\": \"Footer\",\r\n"
	        		+ "                \"label\": \"Continue\",\r\n"
	        		+ "                \"on-click-action\": {\r\n"
	        		+ "                  \"name\": \"complete\",\r\n"
	        		+ "                  \"payload\": {\r\n"
	        		+ "                    \"screen_0_TextInput_0\": \"${form.TextInput_12a893}\",\r\n"
	        		+ "                    \"screen_0_TextInput_1\": \"${form.TextInput_b2dfc2}\",\r\n"
	        		+ "                    \"screen_0_RadioButtonsGroup_2\": \"${form.RadioButtonsGroup_56d4f4}\",\r\n"
	        		+ "                    \"screen_0_DatePicker_3\": \"${form.DatePicker_3b1759}\",\r\n"
	        		+ "                    \"screen_0_DatePicker_4\": \"${form.DatePicker_f8557a}\",\r\n"
	        		+ "                    \"screen_0_TextInput_5\": \"${form.TextInput_0fc35f}\",\r\n"
	        		+ "                    \"screen_0_Dropdown_6\": \"${form.Dropdown_4b9e34}\"\r\n"
	        		+ "                  }\r\n"
	        		+ "                }\r\n"
	        		+ "              }\r\n"
	        		+ "            ]\r\n"
	        		+ "          }\r\n"
	        		+ "        ]\r\n"
	        		+ "      }\r\n"
	        		+ "    }\r\n"
	        		+ "  ]\r\n"
	        		+ "}";

	        try {
	            Map<String, String> fieldMeta = extractFieldMeta(jsonString);
	            fieldMeta.forEach((key, value) -> System.out.println(key + ": " + value));
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

}
