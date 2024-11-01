package com.boot.jx.account.doc;

import java.io.Serializable;

import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;

@Document(collection = "WABA_ACCOUNT_DETAILS")
@TypeAlias("WabaAccountDetails")
public class AccountBalance extends TimeStampDoc implements Serializable {
	long  timestamp;
	String wabaId;
	String number;
	String currency;
	long depositAmt;
}
