package com.boot.jx.admin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;
import com.boot.jx.admin.manager.CSVHelper;

@Service
public class CSVService {
	
	 public CsvDto save(MultipartFile file) {
		    try {
		    	CsvDto dto = CSVHelper.csvToTutorials(file.getInputStream());
		    	 return dto;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to store csv data: " + e.getMessage());
		    }
		    
		  }

}
