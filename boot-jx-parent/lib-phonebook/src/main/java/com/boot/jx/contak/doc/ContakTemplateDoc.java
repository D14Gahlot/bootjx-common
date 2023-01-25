package com.boot.jx.contak.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.contak.dto.ContakModel;
import com.boot.jx.contak.dto.ContakTemplate;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampIndexSupport;

@Document(collection = "CONTAK_TEMPLATE")
public class ContakTemplateDoc extends ContakTemplate implements UpdatedTimeStampIndexSupport {

	private static final long serialVersionUID = 8829109079862522347L;

	@Id
	public String templateId;

	@Indexed
	public String companyId;

	@Indexed
	public String code;

	public boolean deleted;

	public ContakModel model;

	private TimeStampIndex updated;

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public ContakModel getModel() {
		return model;
	}

	public void setModel(ContakModel model) {
		this.model = model;
	}

	public TimeStampIndex getUpdated() {
		return updated;
	}

	public void setUpdated(TimeStampIndex updated) {
		this.updated = updated;
	}

}
