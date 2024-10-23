package com.boot.jx.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boot.jx.admin.manager.WabaAccountManeger;
import com.boot.jx.api.WabaBalanceDto;
import com.boot.jx.postman.doc.WabaAccountBalanceDoc;

@Service
public class WabaService {
	
	@Autowired
	WabaAccountManeger wabaAccMgr;

	public WabaAccountBalanceDoc addEditAccount(WabaAccountBalanceDoc reqDto) {
		WabaAccountBalanceDoc dto= wabaAccMgr.addEditAccountBalance(reqDto);
		return dto;
	}

	public WabaBalanceDto fetchWabaAccountBalance(long timestamp) {
		
		return wabaAccMgr.fetchWabaAccountBalance(timestamp);
	}

}
