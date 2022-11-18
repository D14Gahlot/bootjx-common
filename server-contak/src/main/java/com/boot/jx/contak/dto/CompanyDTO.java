package com.boot.jx.contak.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompanyDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;
	
	@ApiMockModelProperty(example = "Acme Inc.", required = true)
	public String legalBusinessName;
	
	@ApiMockModelProperty(example = "Acme Inc.", required = true)
	public String displayName;
	
	@ApiMockModelProperty(example = "India", required = true)
	public String countryOfOperation;
	
	
	@ApiMockModelProperty(example = "145 Park View", required = true)
	public String address;
	
	@ApiMockModelProperty(example = "https://www.google.com", required = true)
	public String websiteUrl;
	
	@ApiMockModelProperty(example = "John Doe", required = true)
	public String contactPersonName;
	
	@ApiMockModelProperty(example = "John Doe", required = true)
	public String contactPhoneNumber;
	
	@ApiMockModelProperty(example = "John Doe", required = true)
	public String contactPersonEmailId;
	
	@ApiMockModelProperty(example = "******", required = true)
	public String password;

	@ApiMockModelProperty(example = "xxxxxxxx", required = true)
	public String apiKey;
	
	@ApiMockModelProperty(example = "GMT+5:30", required = true)
	public String timezone;

	@ApiMockModelProperty(example = "922333XXXXX", required = true)
	public String number;
	
	@ApiMockModelProperty(example = "www.google.com/abc.png", required = true)
	public String logoUrl;
}