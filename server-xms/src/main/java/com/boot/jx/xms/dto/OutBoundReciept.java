package com.boot.jx.xms.dto;

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
}
