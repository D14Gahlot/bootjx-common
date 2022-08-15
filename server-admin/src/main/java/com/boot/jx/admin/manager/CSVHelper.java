package com.boot.jx.admin.manager;

	import java.io.BufferedReader;
	import java.io.IOException;
	import java.io.InputStream;
	import java.io.InputStreamReader;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.List;
	import java.util.Map;
	import java.util.Objects;

	import org.apache.commons.csv.CSVFormat;
	import org.apache.commons.csv.CSVParser;
	import org.apache.commons.csv.CSVRecord;
	import org.springframework.web.multipart.MultipartFile;

	public class CSVHelper {
		 public static String TYPE = "text/csv";
		 // static String[] HEADERs = { "Id", "Title", "Description", "Published" };
		  public static boolean hasCSVFormat(MultipartFile file) {
		    if (!TYPE.equals(file.getContentType())) {
		      return false;
		    }
		    return true;
		  }
		  public static List<Map<Object,Object>> csvRead(InputStream is) {
			  
			  List<Map<Object,Object>> lst = new ArrayList<>();
		    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		    		//CSVParser csvParser = new CSVParser(fileReader,CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		    		//CSVParser csvParser = new CSVParser(fileReader,CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		    		
		    		) 
		    {
		    	String header = fileReader.readLine();
		    	String[] columns = null;
		        if (header != null) {
		        	columns = header.split(",");
		            for(int i=0;i<columns.length;i++) {
		            	//System.out.println("columns Header :" +columns[i]);
		            }
		        }else {
		        	 System.out.println("The file has no headers, please ensure it has the correct upload format");
		        }

		        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(fileReader);
		        for (CSVRecord record : records) {
		            //System.out.println("Record #: " + record.getRecordNumber());
		        	Map<Object,Object> map = new HashMap<>();
		        	for(int i = 0; i< columns.length; i++)
		        	{	
		        		//System.out.println("Column Name :"+columns[i]+" \t Value : "+record.get(i));
		        		map.put(columns[i], record.get(i));
		        	}
		        	lst.add(map);
		        }
		    	
		      return lst;
		    } catch (IOException e) {
		      throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		    }
		  }
		}

