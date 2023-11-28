package com.boot.jx.contak;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.boot.jx.contak.cache.OtpAlertEvent;

@Configuration
public class DynamoDBSetup {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBSetup.class);

	@Autowired
	public AmazonDynamoDB amazonDynamoDB;

	@Autowired
	public DynamoDBMapper dynamoDBMapper;

	@PostConstruct
	public void setup() throws Exception {

		try {
			CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(OtpAlertEvent.class);
			tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
			tableRequest.getGlobalSecondaryIndexes().get(0)
					.setProvisionedThroughput(new ProvisionedThroughput(10l, 10l));
			amazonDynamoDB.createTable(tableRequest);
		} catch (Exception e) {
			LOGGER.warn(e.getMessage());
		}

	}
}
