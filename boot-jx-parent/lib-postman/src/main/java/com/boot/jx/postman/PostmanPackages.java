package com.boot.jx.postman;

import java.io.IOException;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.model.CommonFileStream;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;

public class PostmanPackages {

	public static interface ICommonTmplPackage {
		public CommonFile process(CommonFile file, ContactType contactType);

		public String process(String templateContent, Object contact);
	}

	public static interface TemplateResolver {
		public BasicTemplate get(String templateId);

		public BasicTemplate get(CommonTemplateMeta template);
	}

	public static interface MessageClient {
		public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage);
	}

	public static interface Text2Media {

		public String toImage(String text) throws IOException;

		public String toImage(String text, String style, String textId) throws IOException;

		public CommonFileStream toImageFile(String text, String style) throws IOException;
	}

}
