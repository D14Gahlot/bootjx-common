package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.utils.ArgUtil;

@Document(collection = "DICT_QUICK_AXN")
@TypeAlias("QuickAction")
public class QuickAction implements Serializable {
	private static final long serialVersionUID = -5649094988762846983L;

	@Id
	private String id;
	private String title;
	private String category;
	private String code;
	private String action;

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

	public String getAction() {
		return ArgUtil.nonEmpty(code, action);
	}

	public void setAction(String action) {
		this.code = action;
	}

	public String getCode() {
		return ArgUtil.nonEmpty(code, action);
	}

	public void setCode(String code) {
		this.code = code;
	}

}
