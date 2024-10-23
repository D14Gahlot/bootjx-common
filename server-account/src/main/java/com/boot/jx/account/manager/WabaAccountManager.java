package com.boot.jx.account.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.account.doc.AccountBalance;
import com.boot.jx.account.dto.WabaCostReqDto;
import com.boot.jx.mongo.CommonMongoTemplate;

public class WabaAccountManager {

	@Autowired
	CommonMongoTemplate mongoTemplate;
	
	public List<AccountBalance> addeditAccount(WabaCostReqDto req){
		
		return null;
	}
}
