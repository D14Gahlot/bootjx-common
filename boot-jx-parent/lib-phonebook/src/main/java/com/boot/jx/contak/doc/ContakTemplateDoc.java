package com.boot.jx.contak.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.contak.dto.ContakTemplate;

@Document(collection = "CONTAK_TEMPLATE")
public class ContakTemplateDoc extends ContakTemplate {

	private static final long serialVersionUID = 8829109079862522347L;

	@Id
	public String templateId;

	@Indexed
	public String companyId;

	@Indexed
	public String code;

}
