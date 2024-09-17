package com.boot.jx.postman.dto;

import java.io.IOException;

import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ChatSessionDTODeserializer extends JsonDeserializer<ChatSessionDTO> {
	private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public ChatSessionDTO deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        ObjectNode node = jp.getCodec().readTree(jp);
        ChatSessionDTO chatSession = new ChatSessionDTO();
        // Manually set fields
        chatSession.setSessionId(node.get("sessionId").asText());
        chatSession.setStartSessionStamp(ArgUtil.parseAsLong(node.get("startSessionStamp").asLong(), 0l));
        chatSession.setResolveSessionStamp(ArgUtil.parseAsLong(node.get("resolveSessionStamp").asLong(), 0l));
        chatSession.setCloseSessionStamp(ArgUtil.parseAsLong(node.get("closeSessionStamp").asLong(), 0l));
        // Handle other fields similarly...
        return chatSession;
    }
}
