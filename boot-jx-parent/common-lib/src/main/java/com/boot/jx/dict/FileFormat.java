package com.boot.jx.dict;

import java.util.HashMap;
import java.util.Map;

import com.boot.utils.ArgUtil;
import com.boot.utils.EnumType;

public enum FileFormat implements EnumType {
	PDF("application/pdf"), CSV("text/csv"),

	PNG("image/png", FileType.IMAGE), JPEG("image/jpeg", FileType.IMAGE), JPG("image/jpg", FileType.IMAGE),
	BMP("image/bmp", FileType.IMAGE), GIF("image/gif", FileType.IMAGE), TIFF("image/tiff", FileType.IMAGE),
	TIF("image/tif", FileType.IMAGE),

	WEBP("image/webp", FileType.IMAGE),

	MP3("audio/mp3", FileType.AUDIO), aac("audio/aac", FileType.AUDIO), AMR("audio/amr", FileType.AUDIO),

	OGG("audio/ogg", FileType.AUDIO), OGG_PLUS("audio/ogg; codecs=opus", FileType.AUDIO),

	AUDIO_MP4("audio/mp4", FileType.AUDIO), AUDIO_MPEG("audio/mpeg", FileType.AUDIO),

	MP4("video/mp4", FileType.VIDEO), VIDEO_3GPP("video/3gpp", FileType.VIDEO),

	JSON("application/json"), HTML("text/html"), TEXT("text/plain", FileType.TEXT), UNKNOWN("application/octet-stream");

	private static final Map<String, FileFormat> TYPEMAP = new HashMap<String, FileFormat>();

	static {
		for (FileFormat fileFormat : FileFormat.values()) {
			TYPEMAP.put(fileFormat.getContentType(), fileFormat);
			// application/vnd.openxmlformats-officedocument.wordprocessingml.document
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
		return TYPEMAP.getOrDefault(contentType, UNKNOWN);
	}

	public static FileFormat from(String contentType, FileFormat defaultValue) {
		return TYPEMAP.getOrDefault(contentType, ArgUtil.nonEmpty(defaultValue, UNKNOWN));
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