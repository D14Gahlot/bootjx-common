package com.boot.jx.postman;

import com.boot.jx.def.CommonInterfaces.ICommonTemplate;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
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

	public BasicTemplate get(ICommonTemplate template);
    }

    public static interface MessageClient {
	public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage);
    }

}
