package com.boot.jx.postman.model;

import java.io.IOException;

import org.springframework.boot.jackson.JsonComponent;

import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

@JsonComponent
public class ContactableDeserializerDef {

	public static class ContactableDeserializer extends StdDeserializer<Contactable> {
		private static final long serialVersionUID = 1L;

		protected ContactableDeserializer(Class<?> vc) {
			super(vc);
		}

		public ContactableDeserializer() {
			this(null);
		}

		@Override
		public Contactable deserialize(JsonParser jp, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			JsonNode jsonNode = jp.getCodec().readTree(jp);
			String text = jsonNode.asText();
			ContactMeta map = null;
			if (ArgUtil.isEmpty(text)) {
				map = JsonUtil.getMapper().convertValue(jsonNode, ContactMeta.class);
			}
			return map;
		}
	}

	public static class ContactMetaKeyDeserializer extends KeyDeserializer {
		@Override
		public Object deserializeKey(String key, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {
			return JsonUtil.getMapper().readValue(key, ContactMeta.class);
		}
	}

	public static class ContactableJsonDeserializer extends JsonDeserializer<Contactable> {
		@Override
		public Contactable deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
			JsonNode jsonNode = jp.getCodec().readTree(jp);
			String text = jsonNode.asText();
			return JsonUtil.getMapper().convertValue(text, ContactMeta.class);
		}
	}
//
//	public static class ContactableEditor extends PropertyEditorSupport {
//
//		private ObjectMapper objectMapper;
//
//		public ContactableEditor(ObjectMapper objectMapper) {
//			this.objectMapper = objectMapper;
//		}
//
//		public ContactableEditor() {
//		}
//
//		@Override
//		public void setAsText(String text) throws IllegalArgumentException {
//			if (StringUtils.isEmpty(text)) {
//				setValue(new ContactMeta());
//			} else {
//				ContactMeta prod = JsonUtil.parse(text, ContactMeta.class);
//				setValue(prod);
//			}
//		}
//
//	}

	static {
		ObjectMapper objectMapper = JsonUtil.getMapper();
		SimpleModule module = new SimpleModule();
		module.addKeyDeserializer(ContactMeta.class, new ContactMetaKeyDeserializer());
		// module.addDeserializer(Contactable.class, new ContactableJsonDeserializer());
		// module.addDeserializer(Contactable.class, new ContactableDeserializer());
		objectMapper.registerModule(module);
	}

}
