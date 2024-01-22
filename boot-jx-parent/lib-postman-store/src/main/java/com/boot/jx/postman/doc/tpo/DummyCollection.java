package com.boot.jx.postman.doc.tpo;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.model.MessageReport;
import com.boot.model.MapModel;

@Document(collection = DummyCollection.COLLECTION_NAME)
public class DummyCollection {
	public static final String COLLECTION_NAME = "Dummy Collection";
	//private MessageReport msgreport;
	private static List<MapModel>list;
	
	public static List<MapModel> getList() {
		return list;
	}
	public static void setList(List<MapModel> requestMap) {
		DummyCollection.list = requestMap;
	}
	
	
}
