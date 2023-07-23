package com.boot.jx.contak.cache;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "OtpAlertEventArchive")
public class OtpAlertNotifiedEvent extends OtpAlertEvent {

}
