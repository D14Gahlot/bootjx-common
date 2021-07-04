package com.boot.jx.xms.service;

import org.springframework.stereotype.Component;

import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.xms.dto.DigitalObjectWrapper;

@Component
@TenantScoped
public class ApiService {

	//@Mongo
	
	public <T> void saveDigitalInfo(DigitalObjectWrapper<T> digitalWrapper) {
		
		
	}
}
