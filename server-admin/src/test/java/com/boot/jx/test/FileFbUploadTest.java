package com.boot.jx.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.ExpressionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.boot.utils.FileUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
//import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileFbUploadTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil
			.getInstance();

	private static Logger LOGGER = LoggerFactory
			.getLogger(FileFbUploadTest.class);

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ExpressionException
	 * @throws URISyntaxException
	 * @throws NumberParseException
	 * @throws IOException
	 */
	private static final String FILE_URL = "https://xyz-mehery-data-longterm-20230321170801943500000002.s3.amazonaws.com/demo/quickmedia/34e2b8d9-0bbe-4a75-9364-a1d212705ded/MCmsBv.jpg";
	private static final String UPLOAD_URL = "https://graph.facebook.com/v19.0/605043468421784/uploads";
	private static final String AUTH_TOKEN = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
	public static void main(String[] args)
			throws URISyntaxException, NumberParseException, IOException {

		//InputStream leftStream = FileUtil.getExternalOrInternalResourceAsStream(
			//	"sample/test.json", FileFbUploadTest.class);
//
	//	String leftJson = IoUtils.inputstream_to_string(leftStream);
		// String
		// fileUrl="https://xyz-mehery-data-longterm-20230321170801943500000002.s3.amazonaws.com/crforex/quickmedia/11820524-e5b2-45f1-b7d5-f68ba1b57fcb/pexels-pixabay-60597.jpg";
		// dpwnload and upload by making session using httpclient
		try {

			String fileName = FILE_URL.substring(FILE_URL.lastIndexOf("/") + 1);
			System.out.println("File name: " + fileName);

			byte[] fileData = downloadFile(FILE_URL);

			int fileLength = fileData.length;
			String fileType = determineFileType(FILE_URL);

			String uploadResponse = firstApiCall(fileData, fileName, fileType,
					fileLength, UPLOAD_URL);
			System.out.println(uploadResponse);

			String id = extractIdFromResponse(uploadResponse);
			System.out.println("id is " + id);

			String finalResponse = secondApiCall(id, fileData, fileName,
					fileType);
			System.out.println(finalResponse);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] downloadFile(String fileUrl) throws IOException {
		try (InputStream in = new URL(fileUrl).openStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

			return baos.toByteArray();
		}
	}
	private static String determineFileType(String fileUrl) throws IOException {
		URLConnection connection = new URL(fileUrl).openConnection();
		return connection.getContentType();
	}
	private static String firstApiCall(byte[] fileData, String fileName,
			String fileType, int fileLength, String uploadUrl)
			throws IOException {
		URL url = new URL(uploadUrl + "?file_type=" + fileType + "&file_length="
				+ fileLength);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type",
				"application/octet-stream");
		connection.setRequestProperty("Authorization", "Bearer " + AUTH_TOKEN);
		connection.setRequestProperty("Cookie", "ps_l=1; ps_n=1");

		try (OutputStream os = connection.getOutputStream()) {
			os.write(fileData);
		}

		try (InputStream is = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			return baos.toString();
		}
	}
	private static String extractIdFromResponse(String response)
			throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(response);
		return jsonNode.get("id").asText();
	}
	private static String secondApiCall(String id, byte[] fileData,
			String fileName, String fileType) throws IOException {
		URL url = new URL("https://graph.facebook.com/v19.0/" + id);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("file_offset", "0");
		connection.setRequestProperty("Content-Type", fileType);
		connection.setRequestProperty("Authorization", "OAuth " + AUTH_TOKEN);
		connection.setRequestProperty("Cookie", "ps_l=1; ps_n=1");

		try (OutputStream os = connection.getOutputStream()) {
			os.write(fileData);
		}

		try (InputStream is = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			return baos.toString();
		}
	}

}
