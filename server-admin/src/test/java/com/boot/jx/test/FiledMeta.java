package com.boot.jx.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FiledMeta{

    public static List<Map<String, String>> extractFieldMeta(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonResponse = objectMapper.readValue(jsonString, Map.class);
        return fetchFieldMeta(jsonResponse);
    }

    public static List<Map<String, String>> fetchFieldMeta(Map<String, Object> jsonResponse) {
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

    private static void fetchChildren(List<Map<String, Object>> children, List<Map<String, String>> fieldMeta, int screenIndex) {
        if (children != null) {
            int inputIndex = 0;
            for (Map<String, Object> child : children) {
                String type = (String) child.get("type");

                if ("Form".equals(type)) {
                    // Recursively process children of Form type
                    Object childChildren = child.get("children");
                    if (childChildren instanceof List) {
                        fetchChildren((List<Map<String, Object>>) childChildren, fieldMeta, screenIndex);
                    }
                } else if (isFieldType(type)) {
                    String label = (String) child.get("label");
                    String key = "screen_" + screenIndex + "_" + type + "_" + inputIndex;

                    Map<String, String> meta = new HashMap<>();
                    meta.put("key", key);
                    meta.put("label", label);
                    fieldMeta.add(meta);

                    inputIndex++;
                }
            }
        }
    }

    private static boolean isFieldType(String type) {
        return "TextInput".equals(type) || "RadioButtonsGroup".equals(type) ||
               "DatePicker".equals(type) || "Dropdown".equals(type);
    }

    public static void main(String[] args) {
        String jsonString = "{\n"
                + "  \"version\": \"3.1\",\n"
                + "  \"screens\": [\n"
                + "    {\n"
                + "      \"id\": \"QUESTION_ONE\",\n"
                + "      \"title\": \"Al Mulla Travels - BOT\",\n"
                + "      \"data\": {},\n"
                + "      \"terminal\": true,\n"
                + "      \"layout\": {\n"
                + "        \"type\": \"SingleColumnLayout\",\n"
                + "        \"children\": [\n"
                + "          {\n"
                + "            \"type\": \"Form\",\n"
                + "            \"name\": \"flow_path\",\n"
                + "            \"children\": [\n"
                + "              {\n"
                + "                \"type\": \"TextBody\",\n"
                + "                \"text\": \"Select 'Add content' to start building your form. To add new screens, select 'Add new' in the 'Screens' panel.\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"TextInput\",\n"
                + "                \"label\": \"Origin\",\n"
                + "                \"name\": \"TextInput_12a893\",\n"
                + "                \"required\": true,\n"
                + "                \"input-type\": \"text\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"TextInput\",\n"
                + "                \"label\": \"Destination\",\n"
                + "                \"name\": \"TextInput_b2dfc2\",\n"
                + "                \"required\": true,\n"
                + "                \"input-type\": \"text\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"RadioButtonsGroup\",\n"
                + "                \"label\": \"Choose one\",\n"
                + "                \"required\": true,\n"
                + "                \"name\": \"RadioButtonsGroup_56d4f4\",\n"
                + "                \"data-source\": [\n"
                + "                  {\n"
                + "                    \"id\": \"0_One_way\",\n"
                + "                    \"title\": \"One way\"\n"
                + "                  },\n"
                + "                  {\n"
                + "                    \"id\": \"1_Return\",\n"
                + "                    \"title\": \"Return\"\n"
                + "                  }\n"
                + "                ]\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"DatePicker\",\n"
                + "                \"label\": \"Start Date\",\n"
                + "                \"required\": true,\n"
                + "                \"name\": \"DatePicker_3b1759\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"DatePicker\",\n"
                + "                \"label\": \"Return Date\",\n"
                + "                \"required\": false,\n"
                + "                \"name\": \"DatePicker_f8557a\",\n"
                + "                \"helper-text\": \"If 'One-way' is chosen, skip this field.\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"TextInput\",\n"
                + "                \"label\": \"Number of people\",\n"
                + "                \"name\": \"TextInput_0fc35f\",\n"
                + "                \"required\": true,\n"
                + "                \"input-type\": \"number\"\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"Dropdown\",\n"
                + "                \"label\": \"Type of travel\",\n"
                + "                \"required\": true,\n"
                + "                \"name\": \"Dropdown_4b9e34\",\n"
                + "                \"data-source\": [\n"
                + "                  {\n"
                + "                    \"id\": \"0_First_\",\n"
                + "                    \"title\": \"First \"\n"
                + "                  },\n"
                + "                  {\n"
                + "                    \"id\": \"1_Business_\",\n"
                + "                    \"title\": \"Business \"\n"
                + "                  },\n"
                + "                  {\n"
                + "                    \"id\": \"2_Economy_\",\n"
                + "                    \"title\": \"Economy \"\n"
                + "                  }\n"
                + "                ]\n"
                + "              },\n"
                + "              {\n"
                + "                \"type\": \"Footer\",\n"
                + "                \"label\": \"Continue\",\n"
                + "                \"on-click-action\": {\n"
                + "                  \"name\": \"complete\",\n"
                + "                  \"payload\": {\n"
                + "                    \"screen_0_TextInput_0\": \"${form.TextInput_12a893}\",\n"
                + "                    \"screen_0_TextInput_1\": \"${form.TextInput_b2dfc2}\",\n"
                + "                    \"screen_0_RadioButtonsGroup_2\": \"${form.RadioButtonsGroup_56d4f4}\",\n"
                + "                    \"screen_0_DatePicker_3\": \"${form.DatePicker_3b1759}\",\n"
                + "                    \"screen_0_DatePicker_4\": \"${form.DatePicker_f8557a}\",\n"
                + "                    \"screen_0_TextInput_5\": \"${form.TextInput_0fc35f}\",\n"
                + "                    \"screen_0_Dropdown_6\": \"${form.Dropdown_4b9e34}\"\n"
                + "                  }\n"
                + "                }\n"
                + "              }\n"
                + "            ]\n"
                + "          }\n"
                + "        ]\n"
                + "      }\n"
                + "    }\n"
                + "  ]\n"
                + "}";

        try {
            List<Map<String, String>> fieldMeta = extractFieldMeta(jsonString);
          
            System.out.println(fieldMeta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
