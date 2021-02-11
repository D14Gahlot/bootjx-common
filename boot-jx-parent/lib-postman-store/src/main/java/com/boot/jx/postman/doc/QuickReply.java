package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.utils.ArgUtil;

@Document(collection = "DICT_QUICK_RESP")
@TypeAlias("QuickReply")
public class QuickReply implements Serializable {
	private static final long serialVersionUID = -5649094988762846983L;

	@Id
	private CompositeKey id;

	@Indexed(unique = true)
	private String uniqueId;

	private String message;
	private String template;

	public CompositeKey id() {
		if (!ArgUtil.is(this.id)) {
			this.id = new CompositeKey();
		}
		return id;
	}

	public static class CompositeKey implements Serializable {
		private static final long serialVersionUID = 2466914948472674930L;
		private String subject;
		private String category;

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public CompositeKey getId() {
		return id;
	}

	public void setId(CompositeKey id) {
		this.id = id;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

}
