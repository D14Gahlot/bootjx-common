package com.boot.jx.postman.doc.ticket;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;
import com.boot.jx.mongo.CommonDocInterfaces.ResourceDocument;
import com.boot.jx.mongo.CommonDocInterfaces.ResourceDocumentKeyDeserializer;
import com.boot.jx.postman.store.QuickStore.QuickGalleryItem;
import com.boot.model.TimeModels.TimeStampSupportedModel;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Document(collection = "TICKET_TYPE")
@TypeAlias("CustomerTicketType")
@JsonDeserialize(as = CustomerTicketType.class, keyUsing = ResourceDocumentKeyDeserializer.class)
public class CustomerTicketType extends TimeStampSupportedModel
		implements Serializable, ResourceDocument, ADocumentDTO<CustomerTicketType>, QuickGalleryItem {

	private static final long serialVersionUID = 2845094878124818820L;

	@Id
	private String id;
	private String title;
	private String category;

	private boolean readonly;

	@Indexed(unique = true, sparse = true)
	private String code;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public ADocumentDTO<CustomerTicketType> newInstance() {
		return new CustomerTicketType();
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

}
