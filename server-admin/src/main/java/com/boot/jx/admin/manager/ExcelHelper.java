package com.boot.jx.admin.manager;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
@Component
public class ExcelHelper {
	
	
	 public  List<Map<String, Object>> convertExcelToFormattedString() throws Exception {
    	 Workbook workbook =null;
    	 FileInputStream file =null;
    	  List<Map<String, Object>> lstMaps = new ArrayList<>();
    	try {
    	
    		String fileP="D:\\Project\\M-Y\\M-Y\\customer_profile\\Annexure2_xlsx.xlsx";
        	   file = new FileInputStream(fileP);
               workbook = WorkbookFactory.create(file);
              Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet
        StringBuilder formattedString = new StringBuilder();
      
        // Iterate through each row starting from row 2
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row headerRow = sheet.getRow(0);
            Row dataRow = sheet.getRow(rowIndex);
            
            if (headerRow != null && dataRow != null) {
                // Iterate through each cell in the row
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                	
                	Map<String, Object> map =new HashMap<>();
                	
                    Cell headerCell = headerRow.getCell(i);
                    Cell dataCell = dataRow.getCell(i);
                    
                    if (headerCell != null ) {
                        String header = headerCell.getStringCellValue();
                        
                        String value =null;
                        if(dataCell!=null) {
                        value = dataCell.getStringCellValue();
                        }
                        
                        formattedString.append(header).append(": ").append(value).append(", ");
                        map.put(header, value);
                    }
                    lstMaps.add(map);
                }
                // Add a new line after each row
                formattedString.append("\n");
                
            }
        }
        // Remove the trailing comma and space
        if (formattedString.length() > 0) {
            formattedString.delete(formattedString.length() - 2, formattedString.length());
        }
        System.out.println("lstMaps :"+formattedString.toString());
        return lstMaps;
    
    	
    } catch (Exception e) {
        e.printStackTrace();
    }finally {
    	 workbook.close();
         file.close();
	}
    	return lstMaps;
    }
    
	

}
