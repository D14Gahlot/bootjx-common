package com.boot.jx.admin.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;

	public class CSVHelper {
		 public static String TYPE = "text/csv";
		  public static boolean hasCSVFormat(MultipartFile file) {
		    if (!TYPE.equals(file.getContentType())) {
		      return false;
		    }
		    return true;
		  }
		  public static CsvDto csvToTutorials(String templateId,InputStream is) {
			  CsvDto dto = new CsvDto();
			  List<Map<Object,Object>> lst = new ArrayList<>();
			  List<String> lsterrors = new ArrayList<>();
			  System.out.println("templateId :"+templateId);
		    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		    		//CSVParser csvParser = new CSVParser(fileReader,CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		    		) 
		    {
		    	
		    	
		  
		    	String header = fileReader.readLine();
		    	String[] columns = null;
		        if (header != null) {
		        	columns = header.split(",");
					/*
					 * for(int i=0;i<columns.length;i++) { //System.out.println("columns Header :"
					 * +columns[i]); }
					 */
		        }else {
		        	 String headerErr ="The file has no headers, please ensure it has the correct upload format";
		        	 lsterrors.add(headerErr);
		        }
		        
		    	Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(fileReader);
		        for (CSVRecord record : records) {
		        	long row = record.getRecordNumber();
		        	Map<Object,Object> map = new HashMap<>();
		        	for(int i = 0; i< columns.length; i++)
		        	{	
		        		if(!StringUtils.isBlank(record.get(i))) {
		        			map.put(columns[i].trim(), record.get(i).trim());
		        		}else {
		        			String str ="Row:"+row+" Column :"+(i+1)+" "+columns[i]+" value  is missing"; 
		        			lsterrors.add(str);
		        		}
		        	}
		        	lst.add(map);
		        }
		        
		        dto.setLstMap(lst);
		        
		        if(lst!=null && !lst.isEmpty()) {
		        Map<Object,List<Object>> combined = lst.stream()
		                .flatMap(m -> m.entrySet().stream())
		                .collect(Collectors.groupingBy(Entry::getKey,
		                        Collectors.mapping(Entry::getValue,
		                                Collectors.toList())));

		       // combined.entrySet().forEach(System.out::println);
		        
		        dto.setCsvMap(combined);
		        }
		    	dto.setLstErrors(lsterrors);
		      return dto;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		    }
		  }
		}
