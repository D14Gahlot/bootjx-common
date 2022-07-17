package com.boot.jx.common.doc;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "APP_FAQ")
@TypeAlias("AppFaqDoc")
public class AppFaqDoc {
	
	@Id
	private String id;
	@Indexed(unique = true)
    private String code;
	private String parent;
	private List<String> relatedCodes;
	protected Map<String, Object> translation;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
		public Map<String, Object> getTranslation() {
		return translation;
	}
	public void setTranslation(Map<String, Object> translation) {
		this.translation = translation;
	}
	public List<String> getRelatedCodes() {
		return relatedCodes;
	}
	public void setRelatedCodes(List<String> relatedCodes) {
		this.relatedCodes = relatedCodes;
	}
	
	
}
