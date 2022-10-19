package com.boot.jx.account.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "DOMAIN_SUMMARY_MESSAGE")
@TypeAlias("DomainSummaryMessageDoc")
public class DomainSummaryMessageDoc implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5826494678717418660L;
	
	@Id
	String id;
	String domain;
	String date;
	String channel;
	private Map<String, Object> messageType;
	Map<String, Map<Object, Long>> dateWiseSummaryCount;
	Map<Object,Long> summaryCount;
	Map<Object,Map<Object,Object>> dateWiseCountMap = new HashMap<>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Map<String, Object> getMessageType() {
		return messageType;
	}
	public void setMessageType(Map<String, Object> messageType) {
		this.messageType = messageType;
	}
	public Map<Object, Long> getSummaryCount() {
		return summaryCount;
	}
	public void setSummaryCount(Map<Object, Long> summaryCount) {
		this.summaryCount = summaryCount;
	}
	public Map<String, Map<Object, Long>> getDateWiseSummaryCount() {
		return dateWiseSummaryCount;
	}
	public void setDateWiseSummaryCount(Map<String, Map<Object, Long>> dateWiseSummaryCount) {
		this.dateWiseSummaryCount = dateWiseSummaryCount;
	}
	public Map<Object, Map<Object, Object>> getDateWiseCountMap() {
		return dateWiseCountMap;
	}
	public void setDateWiseCountMap(Map<Object, Map<Object, Object>> dateWiseCountMap) {
		this.dateWiseCountMap = dateWiseCountMap;
	}
	
}
