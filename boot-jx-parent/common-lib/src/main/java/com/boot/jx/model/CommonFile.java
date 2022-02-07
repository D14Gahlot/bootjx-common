package com.boot.jx.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.integration.http.multipart.UploadedMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonFile implements Serializable {

    private static final long serialVersionUID = -254665668785648863L;

    private static Logger LOGGER = LoggerService.getLogger(CommonFile.class);

    public CommonFile lang(Object lang) {
	this.template.setLang(ArgUtil.parseAsString(lang));
	return this;
    }

    private String content;
    private String path;
    private String name;
    private String title;
    protected FileFormat fileFormat;
    private FileType fileType;
    private String extension;
    private String password;
    private String url;
    private CommonTemplate template = null;
    private Map<String, Object> model = new HashMap<String, Object>();
    private Map<String, Object> options = new HashMap<String, Object>();
    private Map<String, String> headers;

    public Map<String, Object> getModel() {
	return model;
    }

    public void setModel(Map<String, Object> model) {
	this.model = model;
    }

    public Map<String, Object> model() {
	if (this.model == null) {
	    this.model = new HashMap<String, Object>();
	}
	return model;
    }

    @JsonIgnore
    public void setObject(Object object) {
	this.model = JsonUtil.toMap(object);
    }

    protected byte[] body;

    public FileFormat getFileFormat() {
	return fileFormat;
    }

    public void setFileFormat(FileFormat type) {
	this.fileFormat = type;
    }

    public byte[] getBody() {
	return body;
    }

    public void setBody(byte[] body) {
	this.body = body;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getContent() {
	return content;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public void setContent(String content) {
	this.content = content;
    }

    public CommonTemplate getTemplate() {
	return template;
    }

    public void setTemplate(CommonTemplate template) {
	this.template = template;
    }

    @JsonSetter
    public CommonTemplate template() {
	if (!ArgUtil.is(this.template)) {
	    this.template = new CommonTemplate();
	}
	return this.template;
    }

    public CommonFile template(CommonTemplate template) {
	this.template = template;
	return this;
    }

    public CommonFile template(String template) {
	this.template().setCode(template);
	return this;
    }

    public CommonFile templateId(String templateId) {
	this.template().setCode(templateId);
	return this;
    }

    /**
     * If file type is json, this should return valid map
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
	return JsonUtil.fromJson(this.content, Map.class);
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public Map<String, Object> getOptions() {
	return options;
    }

    public void setOptions(Map<String, Object> options) {
	this.options = options;
    }

    public Map<String, Object> options() {
	if (this.options == null) {
	    this.options = new HashMap<String, Object>();
	}
	return options;
    }

    public FileType getFileType() {
	if (ArgUtil.is(this.fileType)) {
	    return this.fileType;
	} else if (ArgUtil.is(this.fileFormat)) {
	    return this.fileFormat.getFileType();
	}
	return this.fileType;
    }

    public String getContentType() {
	if (ArgUtil.is(this.fileFormat)) {
	    return this.fileFormat.getContentType();
	}
	return null;
    }

    public void setExtension(String extension) {
	this.extension = extension;
    }

    public String getExtension() {
	if (ArgUtil.is(this.extension)) {
	    return this.extension;
	} else if (ArgUtil.is(this.fileFormat)) {
	    return this.fileFormat.name().toLowerCase();
	}
	return this.extension;
    }

    public void setFileType(FileType fileType) {
	this.fileType = fileType;
    }

    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public Map<String, String> headers() {
	if (!ArgUtil.is(this.headers)) {
	    this.headers = new HashMap<String, String>();
	}
	return this.headers;
    }

    public CommonFile url(String url) {
	this.setUrl(url);
	try {
	    URL urlObject = new URL(url);
	    if (!ArgUtil.is(this.extension)) {
		this.extension = FilenameUtils.getExtension(urlObject.getPath());
	    }
	    if (!ArgUtil.is(this.name)) {
		this.name = FilenameUtils.getName(urlObject.getPath());
	    }
	    if (!ArgUtil.is(this.title)) {
		this.title = FilenameUtils.getBaseName(urlObject.getPath());
	    }
	    if (!ArgUtil.is(this.fileFormat) && ArgUtil.is(this.name)) {
		this.fileFormat = FileFormat.from(URLConnection.guessContentTypeFromName(name));
	    }

	} catch (MalformedURLException e) {
	    e.printStackTrace();
	}
	return this;
    }

    public CommonFile contentType(String contentType) {
	this.fileFormat = FileFormat.from(contentType, this.fileFormat);
	if (ArgUtil.is(this.fileFormat)) {
	    this.fileType = this.fileFormat.getFileType();
	}
	return this;
    }

    @Deprecated
    public CommonFile type(FileFormat fileFormat) {
	this.setFileFormat(fileFormat);
	return this;
    }

    public CommonFile fileType(FileType fileType) {
	this.setFileType(fileType);
	return this;
    }

    public CommonFile format(FileFormat format) {
	this.setFileFormat(format);
	return this;
    }

    public CommonFile path(String path) {
	this.setPath(path);
	return this;
    }

    public CommonFile name(String name) {
	this.setName(name);
	return this;
    }

    public CommonFile header(String headerKey, String headerValue) {
	this.headers().put(headerKey, headerValue);
	return this;
    }

    // Common Methoeds
    public void create(HttpServletResponse response, Boolean download) throws IOException {
	OutputStream outputStream = null;
	response.setHeader("Cache-Control", "cache, must-revalidate");
	if (this.fileFormat == FileFormat.PDF) {
	    response.addHeader("Content-type", "application/pdf");
	} else if (this.fileFormat == FileFormat.PNG) {
	    response.addHeader("Content-type", "application/pdf");
	} else if (this.fileFormat == FileFormat.JPEG) {
	    response.addHeader("Content-type", "application/jpeg");
	} else if (this.fileFormat == FileFormat.JPG) {
	    response.addHeader("Content-type", "application/jpg");
	}
	if (download) {
	    response.addHeader("Content-Disposition", "attachment; filename=" + getName());
	}
	try {
	    if (body != null) {
		outputStream = response.getOutputStream();
		outputStream.write(this.body);
		LOGGER.info("PDF created successfully :  Template {}", this.getTemplate());
	    }
	} finally {
	    if (outputStream != null) {
		try {
		    outputStream.close();
		} catch (IOException e) {
		    /* ignore */ }
	    }
	}
    }

    public static CommonFile fromBase64(String base64String) {
	return fromBase64(base64String, FileFormat.TEXT);
    }

    public static CommonFile fromBase64(String base64String, FileFormat defaultType) {
	String[] strings = base64String.split(",");
	FileFormat extension;
	String dataPart;
	if (strings.length > 1) {
	    dataPart = strings[1];
	    switch (strings[0]) {// check image's extension
	    case "data:image/jpeg;base64":
		extension = FileFormat.JPEG;
		break;
	    case "data:image/png;base64":
		extension = FileFormat.PNG;
		break;
	    default:// should write cases for more images types
		extension = FileFormat.JPG;
		break;
	    }
	} else {
	    extension = defaultType;
	    dataPart = strings[0];
	}
	CommonFile file = new CommonFile();
	file.setFileFormat(extension);
	file.setBody(DatatypeConverter.parseBase64Binary(dataPart));
	return file;
    }

    public MultipartFile toMultipartFile() {
	try {
	    URL url = new URL(this.url);
	    URLConnection connection = url.openConnection();

	    if (ArgUtil.is(this.headers)) {
		for (Entry<String, String> entry : this.headers.entrySet()) {
		    connection.setRequestProperty(entry.getKey(), entry.getValue());
		}
	    }

	    InputStream inputStream = connection.getInputStream();
	    File file = File.createTempFile("tmp", "." + this.getExtension());
	    byte[] binary = IOUtils.toByteArray(inputStream);
	    FileUtils.writeByteArrayToFile(file, binary);

	    String mimeType = this.getContentType();
	    if (!ArgUtil.is(this.getFileFormat())) {
		InputStream is = new BufferedInputStream(new FileInputStream(file));
		mimeType = URLConnection.guessContentTypeFromStream(is);
		this.setFileFormat(FileFormat.from(mimeType));
	    }

	    UploadedMultipartFile multipartFile = new UploadedMultipartFile(file, file.length(), mimeType,
		    "formParameter", this.getName());
	    return multipartFile;
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;
    }

    public Map<String, String> getHeaders() {
	return headers;
    }

    public void setHeaders(Map<String, String> headers) {
	this.headers = headers;
    }

    public CommonFile headers(Map<String, String> headers) {
	this.headers = headers;
	return this;
    }

}
