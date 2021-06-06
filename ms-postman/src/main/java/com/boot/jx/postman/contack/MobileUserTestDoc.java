package com.boot.jx.postman.contack;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileUserTestDoc implements Serializable {

	private static final long serialVersionUID = -2799840181636380924L;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	private String date;

	@ApiMockModelProperty(example = "PCR", required = false, allowableValues = "PCR,NTBODY,NTGEN")
	private String type;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	private Boolean positive;

	@ApiMockModelProperty(example = "H", required = false, allowableValues = "H,M,L")
	private String severity;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getPositive() {
		return positive;
	}

	public void setPositive(Boolean positive) {
		this.positive = positive;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}
}