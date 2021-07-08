package com.boot.jx.xms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.xms.dto.DigitalDocStore;
import com.boot.jx.xms.dto.DigitalObjectDto;


@Component
public class ApiService {


	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	DigitalDocStore digitalDocStore;
	
	
	public  void saveDigitalInfo(DigitalObjectDto digitalObjectDto) {
		digitalDocStore.createAndUpdateDigitalDoc(digitalObjectDto);
	}
	
	
	
	
}
