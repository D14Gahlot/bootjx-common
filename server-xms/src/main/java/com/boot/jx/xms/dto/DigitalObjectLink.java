package com.boot.jx.xms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalObjectLink
{
	String linkName;
	String linkType;
	String linkValue;
	public String getLinkName()
	{
		return linkName;
	}
	public void setLinkName(String linkName)
	{
		this.linkName = linkName;
	}
	public String getLinkType()
	{
		return linkType;
	}
	public void setLinkType(String linkType)
	{
		this.linkType = linkType;
	}
	public String getLinkValue()
	{
		return linkValue;
	}
	public void setLinkValue(String linkValue)
	{
		this.linkValue = linkValue;
	}
}
