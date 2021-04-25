package com.boot.jx.admin.dto;

import java.util.HashMap;
import java.util.Map;

public class TagDocumentDto {
	Map<String,TagDocumentLst> mapTagDocument= new HashMap<String,TagDocumentLst>();

	public Map<String, TagDocumentLst> getMapTagDocument() {
		return mapTagDocument;
	}

	public void setMapTagDocument(Map<String, TagDocumentLst> mapTagDocument) {
		this.mapTagDocument = mapTagDocument;
	}
}
