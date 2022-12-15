package com.boot.jx.contak.dto;

import java.io.Serializable;
import java.util.List;

public class ContakTemplate implements Serializable {

	private static final long serialVersionUID = -6039318960935942053L;

	public static class ContakTemplateHeader implements Serializable {
		private static final long serialVersionUID = 825821221048380985L;
		public String label;
		public String variant;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getVariant() {
			return variant;
		}

		public void setVariant(String variant) {
			this.variant = variant;
		}
	}

	public static class ContakTemplateCTA implements Serializable {
		private static final long serialVersionUID = -2966964156450843043L;
		public String label;
		public String phone;
		public String url;
		public String variant;

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getVariant() {
			return variant;
		}

		public void setVariant(String variant) {
			this.variant = variant;
		}
	}

	public ContakTemplateHeader header;

	public String category;
	public String type;
	public String title;
	public String body;
	public String footer;
	public List<ContakTemplateCTA> cta;

	public ContakTemplateHeader getHeader() {
		return header;
	}

	public void setHeader(ContakTemplateHeader header) {
		this.header = header;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public List<ContakTemplateCTA> getCta() {
		return cta;
	}

	public void setCta(List<ContakTemplateCTA> cta) {
		this.cta = cta;
	}

}
