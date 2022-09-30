package com.boot.jx.admin.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;
import com.boot.jx.admin.manager.CSVHelper;

@Service
public class CSVService {
	
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	CSVHelper csvHelper;
	
	 public CsvDto save(String templateId,MultipartFile file) {
		    try {
		    	CsvDto dto = csvHelper.csvToTutorials(templateId,file.getInputStream());
		    	if(dto.getLstErrors().isEmpty()) {
		    	 mongoTemplate.save(dto);
		    	}
		    	return dto;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to store csv data: " + e.getMessage());
		    }
		    
		  }

}
