package com.boot.jx.postman.model.ext;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * https://developers.facebook.com/docs/whatsapp/api/webhooks
 * 
 * @author lalittanwar
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InBoundWrapper implements Serializable {
    private static final long serialVersionUID = 7766790295486098869L;

    public Map<String, Object> meta;

    public List<InBoundContact> contacts;
    public List<InBoundMsg> messages;
    public List<InBoundAction> actions;
}
