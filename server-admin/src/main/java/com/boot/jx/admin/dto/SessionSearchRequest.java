package com.boot.jx.admin.dto;

import java.util.List;

import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.doc.QuickTag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionSearchRequest {
	public String text;
	public List<CHAT_STATUS> status;
	public List<QuickTag> tags;
	public long fromStamp;
	public long toStamp;
	public long limit;
	
}
