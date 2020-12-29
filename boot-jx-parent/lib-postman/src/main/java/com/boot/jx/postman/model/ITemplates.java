package com.boot.jx.postman.model;

import com.boot.jx.ProjectConfig;
import com.boot.jx.dict.Project;
import com.boot.jx.postman.model.File.PDFConverter;
import com.boot.jx.postman.model.Notipy.ChannelType;
import com.boot.utils.ArgUtil;

public class ITemplates {

	public interface ITemplate {

		boolean isThymleaf();

		public default boolean isGeneric() {
			return false;
		};

		PDFConverter getConverter();

		String getFileName();

		public default ChannelType getChannel() {
			return null;
		}

		public default String getSampleJSON() {
			return this.getFileName() + ".json";
		}

		public default String getHtmlFile() {
			return "html/" + getFileName();
		}

		public default String getSMSFile() {
			return "html/sms/" + getFileName();
		}

		public default String getJsonFile() {
			return "json/" + getFileName();
		}

	}

	public static ITemplate getTemplate(String templateStr) {
		ITemplate t = getTemplateInternal(templateStr);
		if (t == null) {
			return new TemplateGeneric(templateStr);
		}
		return t;
	}

	private static ITemplate getTemplateInternal(String templateStr) {
		if (ProjectConfig.PROJECT == Project.IB) {
			return (ITemplate) ArgUtil.parseAsEnum(templateStr, TemplatesIB.class);
		}
		return (ITemplate) ArgUtil.parseAsEnum(templateStr, TemplatesMX.class);
	}

	public static class TemplateGeneric implements ITemplate {

		String fileName;

		public TemplateGeneric(String fileName) {
			this.fileName = fileName;
		}

		@Override
		public boolean isThymleaf() {
			return true;
		}

		@Override
		public boolean isGeneric() {
			return true;
		}

		@Override
		public PDFConverter getConverter() {
			return null;
		}

		@Override
		public String getFileName() {
			return this.fileName;
		};

		@Override
		public String toString() {
			return this.fileName;
		};

		@Override
		public String getSampleJSON() {
			return "template-generic.json";
		}

	}

}
