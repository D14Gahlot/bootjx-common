package com.boot.jx.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * The Class WebApplication.
 * 
 * @EnableTransactionManagement
 * @EnableCaching
 */
@Configuration
@PropertySource("classpath:application-aws.properties")
@EnableEncryptableProperties
public class AWSConfig {

	@Value("${aws.s3.b1.accessKey}")
	String amazoneS3AccessKey;

	@Value("${aws.s3.b1.secretKey}")
	String amazoneS3secretKey;

	@Value("${aws.s3.b1.region}")
	String amazoneS3region;

	@Value("${aws.s3.b1.bucket}")
	String amazoneS3bucket;

	AmazonS3 s3b1;

	public AmazonS3 getS3B1() {
		if (s3b1 == null) {
			AWSCredentials awsCredentials = new BasicAWSCredentials(amazoneS3AccessKey, amazoneS3secretKey);
			s3b1 = AmazonS3ClientBuilder.standard().withRegion(amazoneS3region)
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
		}
		return s3b1;
	}

	public String getS3B1Name() {
		return amazoneS3bucket;
	}

	@Value("${aws.s3.b2.accessKey}")
	String amazoneS3AccessKey2;

	@Value("${aws.s3.b2.secretKey}")
	String amazoneS3secretKey2;

	@Value("${aws.s3.b2.region}")
	String amazoneS3region2;

	@Value("${aws.s3.b2.bucket}")
	String amazoneS3bucket2;

	AmazonS3 s3b2;

	public AmazonS3 getS3B2() {
		if (s3b2 == null) {
			AWSCredentials awsCredentials = new BasicAWSCredentials(amazoneS3AccessKey2, amazoneS3secretKey2);
			s3b2 = AmazonS3ClientBuilder.standard().withRegion(amazoneS3region2)
					.withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).build();
		}
		return s3b2;
	}

	public String getS3B2Name() {
		return amazoneS3bucket2;
	}

}
