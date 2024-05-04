package com.boot.jx.ioutbound;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;

/**
 * https://developers.facebook.com/docs/whatsapp/api/errors
 * 
 * @author lalittanwar
 *
 */
public class OutBoundReciept implements JsonIgnoreUnknown {

	private static final long serialVersionUID = 6528883942938374590L;
	@ApiMockModelProperty(example = "gBEGkYiEB1VXAglK1ZEqA1YKPrU")
	public String id;
	public String type;
	public String templateCode;
	public String templateId;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OutBoundReciept id(String id) {
		this.id = id;
		return this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTemplateCode() {
		return templateCode;
	}

	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

}
