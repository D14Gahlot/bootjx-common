package com.boot.jx.admin.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagDocumentDto {
	
	List<TagDocumentLst> lstTagDocument= new ArrayList<TagDocumentLst>();
	
	Map<String,TagDocumentLst> mapTagDocument= new HashMap<String,TagDocumentLst>();

	public Map<String, TagDocumentLst> getMapTagDocument() {
		return mapTagDocument;
	}

	public void setMapTagDocument(Map<String, TagDocumentLst> mapTagDocument) {
		this.mapTagDocument = mapTagDocument;
	}

	public List<TagDocumentLst> getLstTagDocument() {
		return lstTagDocument;
	}

	public void setLstTagDocument(List<TagDocumentLst> lstTagDocument) {
		this.lstTagDocument = lstTagDocument;
	}
}
