package com.boot.jx.admin.dto;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.common.dto.DepartmentResponseDto;
import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;

public class CampaignDTO <T extends CampaignDTO<T>> implements ADocumentDTO<T> {
   @Autowired
	private ChatSessionDTO chatsessionDto;
	private String campaignId;
	private String campaignName;
	private boolean isActive;
	//private Map<String,String>info;
	
	public ChatSessionDTO getChatsessionDto() {
		return chatsessionDto;
	}
	public void setChatsessionDto(ChatSessionDTO chatsessionDto) {
		this.chatsessionDto = chatsessionDto;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getCampaignName() {
		return campaignName;
	}
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}
	
	@Override
	public CampaignDTO newInstance() {
		return new CampaignDTO();
	}

}
