package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;

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

    public List<InBoundContact> contacts;
    public List<InBoundMsg> messages;
    public List<InBoundEvent> events;
}
