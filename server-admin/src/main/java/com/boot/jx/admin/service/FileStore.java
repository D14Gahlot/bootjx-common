package com.boot.jx.admin.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.model.File.FileFormat;

@Component
public class FileStore {

	@Value("${aws.s3.bucket}")
	String amazoneS3bucket;

	@Autowired
	private AmazonS3 amazonS3;

	public void upload(String path, String fileName, Optional<Map<String, String>> optionalMetaData,
			InputStream inputStream) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		optionalMetaData.ifPresent(map -> {
			if (!map.isEmpty()) {
				map.forEach(objectMetadata::addUserMetadata);
			}
		});
		try {
			amazonS3.putObject(path, fileName, inputStream, objectMetadata);
		} catch (AmazonServiceException e) {
			throw new IllegalStateException("Failed to upload the file", e);
		}
	}

	public String saveTodo(MultipartFile file) {
		// check if the file is empty
		if (file.isEmpty()) {
			throw new IllegalStateException("Cannot upload empty file");
		}

		// Check if the file is an image
		if (!Arrays
				.asList(FileFormat.PNG.getContentType(), FileFormat.BMP.getContentType(),
						FileFormat.GIF.getContentType(), FileFormat.JPEG.getContentType())
				.contains(file.getContentType())) {
			throw new IllegalStateException("FIle uploaded is not an image");
		}
		// get file metadata
		Map<String, String> metadata = new HashMap<>();
		metadata.put("Content-Type", file.getContentType());
		metadata.put("Content-Length", String.valueOf(file.getSize()));
		// Save Image in S3 and then save Todo in the database
		String pathFolder = String.format("%s/%s", AppContextUtil.getTenant(), UUID.randomUUID());

		String path = String.format("%s/%s", amazoneS3bucket, pathFolder);
		String fileName = String.format("%s", file.getOriginalFilename());
		try {
			upload(path, fileName, Optional.of(metadata), file.getInputStream());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to upload file", e);
		}
		return String.format("http://%s.s3.amazonaws.com/%s/%s", amazoneS3bucket, pathFolder, fileName);
	}

	public byte[] download(String path, String key) {
		try {
			S3Object object = amazonS3.getObject(path, key);
			S3ObjectInputStream objectContent = object.getObjectContent();
			return IOUtils.toByteArray(objectContent);
		} catch (AmazonServiceException | IOException e) {
			throw new IllegalStateException("Failed to download the file", e);
		}
	}
}
