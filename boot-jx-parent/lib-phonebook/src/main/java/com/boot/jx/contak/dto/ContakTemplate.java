package com.boot.jx.contak.dto;

import java.io.Serializable;
import java.util.List;

public class ContakTemplate implements Serializable {

	private static final long serialVersionUID = -6039318960935942053L;

	public static class ContakTemplateHeader {
		public String label;
		public String variant;
	}

	public static class ContakTemplateCTA {
		public String label;
		public String phone;
		public String url;
		public String variant;
	}

	public ContakTemplateHeader header;

	public String category;
	public String type;
	public String title;
	public String body;
	public String footer;
	public List<ContakTemplateCTA> cta;

}
