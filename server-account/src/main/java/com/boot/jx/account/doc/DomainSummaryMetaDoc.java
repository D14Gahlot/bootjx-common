package com.boot.jx.account.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.validation.AlphaNumValidator.ValidAlphaNum;

@Document(collection = "DOMAIN_SUMMARY_META")
@TypeAlias("DomainSummaryMetaDoc")
public class DomainSummaryMetaDoc implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8077128611696133877L;
	
	@Id
    @ValidAlphaNum
	String domain;
	long timeStamp;
	long domainUpdatedStamp;
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public long getDomainUpdatedStamp() {
		return domainUpdatedStamp;
	}
	public void setDomainUpdatedStamp(long domainUpdatedStamp) {
		this.domainUpdatedStamp = domainUpdatedStamp;
	}
	
	

}
