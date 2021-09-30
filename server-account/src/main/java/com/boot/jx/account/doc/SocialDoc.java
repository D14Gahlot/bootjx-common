package com.boot.jx.account.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;

public class SocialDoc implements Serializable {

    private static final long serialVersionUID = -3354844112176554561L;

    private String facebookBMId;
    private String facebookPageId;
    private String facebookPage;
    private String twitterHandler;
    private String instagramHandler;

    public String getFacebookBMId() {
	return facebookBMId;
    }

    public void setFacebookBMId(String facebookBMId) {
	this.facebookBMId = facebookBMId;
    }

    public String getFacebookPageId() {
	return facebookPageId;
    }

    public void setFacebookPageId(String facebookPageId) {
	this.facebookPageId = facebookPageId;
    }

    public String getFacebookPage() {
	return facebookPage;
    }

    public void setFacebookPage(String facebookPage) {
	this.facebookPage = facebookPage;
    }

    public String getTwitterHandler() {
	return twitterHandler;
    }

    public void setTwitterHandler(String twitterHandler) {
	this.twitterHandler = twitterHandler;
    }

    public String getInstagramHandler() {
	return instagramHandler;
    }

    public void setInstagramHandler(String instagramHandler) {
	this.instagramHandler = instagramHandler;
    }
}
