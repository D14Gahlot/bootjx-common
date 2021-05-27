package com.boot.jx.aws;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.model.CommonFile;
import com.boot.utils.ArgUtil;

@Component
public class AWSFileStore {

	@Autowired
	private AWSConfig awsConfig;

	public void upload(AmazonS3 amazonS3, String path, String fileName, ObjectMetadata objectMetadata,
			InputStream inputStream) {
		try {
			amazonS3.putObject(path, fileName, inputStream, objectMetadata);
		} catch (AmazonServiceException e) {
			throw new IllegalStateException("Failed to upload the file", e);
		}
	}

	private CommonFile createFile(AmazonS3 amazonS3, String bucketName, String pathFolder, String fileName,
			String contentType) {
		FileFormat fileFormat = FileFormat.from(contentType);

		// Save Image in S3 and then save Todo in the database

		String fileNameNow = String.format("%s", fileName);

		return new CommonFile()
				.url(String.format("http://%s.s3.amazonaws.com/%s/%s", bucketName, pathFolder, fileNameNow))
				.path(pathFolder).name(fileNameNow).format(fileFormat);
	}

	private CommonFile commitFile(AmazonS3 amazonS3, String bucketName, CommonFile dstFile, MultipartFile file) {
		// check if the file is empty
		if (file.isEmpty()) {
			throw new IllegalStateException("Cannot upload empty file");
		}

		FileFormat fileFormat = FileFormat.from(file.getContentType());
		dstFile.setFileFormat(fileFormat);

		// Check if the file is an image
		if (!ArgUtil.is(dstFile.getFileFormat())) {
			throw new IllegalStateException("File uploaded is not an accepted format");
		}


		// get file metadata
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(file.getContentType());
		objectMetadata.setContentLength(file.getSize());

		// Save Image in S3 and then save Todo in the database
		String bucketPath = String.format("%s/%s", bucketName, dstFile.getPath());

		try {
			upload(amazonS3, bucketPath, dstFile.getName(), objectMetadata, file.getInputStream());
		} catch (IOException e) {
			throw new IllegalStateException("Failed to upload file", e);
		}

		return dstFile;
	}

	private CommonFile upload(AmazonS3 amazonS3, String bucketName, String pathFolder, String fileName,
			MultipartFile file) {
		CommonFile dstFile = createFile(amazonS3, bucketName, pathFolder, fileName, file.getContentType());
		return commitFile(amazonS3, bucketName, dstFile, file);
	}

	public CommonFile upload1(MultipartFile file, String pathFolder, String fileName) {
		return upload(awsConfig.getS3B1(), awsConfig.getS3B1Name(), pathFolder, fileName, file);
	}

	public CommonFile upload2(MultipartFile file, String pathFolder, String fileName) {
		return upload(awsConfig.getS3B2(), awsConfig.getS3B2Name(), pathFolder, fileName, file);
	}

	public CommonFile createFile2(CommonFile srcFile, String pathFolder, String fileName) {
		return createFile(awsConfig.getS3B2(), awsConfig.getS3B2Name(), pathFolder, fileName, srcFile.getContentType());
	}

	@Async
	public CommonFile commitFile2(MultipartFile srcFile, CommonFile dstFile) {
		return commitFile(awsConfig.getS3B2(), awsConfig.getS3B2Name(), dstFile, srcFile);
	}

	@Async
	public CommonFile commitFile2(CommonFile srcFile, CommonFile dstFile) {
		MultipartFile srcMultipartFile = new CommonFile().url(srcFile.getUrl()).format(dstFile.getFileFormat())
				.name(dstFile.getName()).toMultipartFile();
		return commitFile(awsConfig.getS3B2(), awsConfig.getS3B2Name(), dstFile, srcMultipartFile);
	}

	public byte[] download1(String pathFolder, String key) {
		try {
			String path = String.format("%s/%s", awsConfig.getS3B1Name(), pathFolder);
			S3Object object = awsConfig.getS3B1().getObject(path, key);
			S3ObjectInputStream objectContent = object.getObjectContent();
			return IOUtils.toByteArray(objectContent);
		} catch (AmazonServiceException | IOException e) {
			throw new IllegalStateException("Failed to download the file", e);
		}
	}

}
