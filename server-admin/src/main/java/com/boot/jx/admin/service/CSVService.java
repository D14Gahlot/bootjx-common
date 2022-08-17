package com.boot.jx.admin.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.manager.CSVHelper;

@Service
public class CSVService {
	
	  public List<Map<Object,Object>> save(MultipartFile file) {
	    try {
	    	List<Map<Object,Object>> lst = CSVHelper.csvRead(file.getInputStream());
	    	 return lst;
	    } catch (IOException e) {
	      throw new RuntimeException("fail to store csv data: " + e.getMessage());
	    }
	    
	  }

}
