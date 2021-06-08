package com.boot.jx.postman.doc;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = MessageDocWA.COLLECTION_NAME + "_WHATSAPP")
@TypeAlias("MessageDoc")
public class MessageDocWA extends MessageDoc {

	private static final long serialVersionUID = -2175867087327783840L;
	
}
