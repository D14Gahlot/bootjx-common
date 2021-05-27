package com.boot.jx.dict;

import java.util.HashMap;
import java.util.Map;

import com.boot.utils.EnumType;

public enum FileFormat implements EnumType {
	PDF("application/pdf"), CSV("text/csv"),

	PNG("image/png", FileType.IMAGE), JPEG("image/jpeg", FileType.IMAGE), JPG("image/jpg", FileType.IMAGE),
	BMP("image/bmp", FileType.IMAGE), GIF("image/gif", FileType.IMAGE), TIFF("image/tiff", FileType.IMAGE),
	TIF("image/tif", FileType.IMAGE),

	JSON("application/json"), HTML("text/html"), TEXT("text/plain", FileType.TEXT);

	private static final Map<String, FileFormat> TYPEMAP = new HashMap<String, FileFormat>();

	static {
		for (FileFormat fileFormat : FileFormat.values()) {
			TYPEMAP.put(fileFormat.getContentType(), fileFormat);
		}
	}

	String contentType;
	FileType fileType;

	public String getContentType() {
		return contentType;
	}

	FileFormat(String contentType, FileType formatType) {
		this.contentType = contentType;
		this.fileType = formatType;

	}

	FileFormat(String contentType) {
		this(contentType, FileType.DOCUMENT);
	}

	public static FileFormat from(String contentType) {
		return TYPEMAP.get(contentType);
	}

	/**
	 * IMAGE,TEXT, DOCUMENT,VIDEO
	 * 
	 * @return
	 */
	public FileType getFileType() {
		return fileType;
	}

}