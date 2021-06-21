package com.boot.jx.postman;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PostManFile;

public class PostmanPackages {

	public static interface ICommonTmplPackage {
		public PostManFile process(PostManFile file, ContactType contactType);

		public String process(String templateContent, Object contact);
	}

	public static interface MessageClient {
		public OutboxMessage send(OutboxMessage outboxMessage);
	}

}
