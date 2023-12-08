package com.boot.jx.admin.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.FILE_TYPE;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import springfox.documentation.swagger.web.SwaggerApiListingReader;

@Component
public class CSVHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(CSVHelper.class);

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	public static boolean hasCSVFormat(MultipartFile file) {
		if (!FILE_TYPE.CSV.equals(file.getContentType())) {
			return false;
		}
		return true;
	}

	public static boolean hasExcelFormat(MultipartFile file) {
		if (FILE_TYPE.EXCEL.equals(file.getContentType()) ||
			FILE_TYPE.XLS.equals(file.getContentType())) {
			return true;
		}
		return false;
	}
	
	public static boolean hasExcelSXFormat(MultipartFile file) {
		if (FILE_TYPE.XLSX.equals(file.getContentType())) {
			return true;
		}
		return false;
	}
	
	

	public CsvDto csvToTutorials(String templateId, InputStream is) throws IOException {
		CsvDto dto = new CsvDto();
		BufferedReader fileReader = null;
		List<Map<Object, Object>> lst = new ArrayList<>();
		List<String> lsterrors = new ArrayList<>();
		dto.setTemplateId(templateId);
		List<String> templVarLst = fetchTemplateHeader(templateId);
		try {
			fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String header = fileReader.readLine();
			String[] columns = null;
			if (header != null) {
				columns = header.split(",");
				for (int i = 0; i < columns.length; i++) {
					if (!templVarLst.contains(columns[i])) {
						String colHeadErr = "The selected template does not have a header " + columns[i];
						lsterrors.add(colHeadErr);
					}
				}

			} else {
				String headerErr = "The file has no headers, please ensure it has the correct upload format";
				lsterrors.add(headerErr);
			}

			Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(fileReader);
			for (CSVRecord record : records) {
				long row = record.getRecordNumber();
				Map<Object, Object> map = new HashMap<>();
				for (int i = 0; i < columns.length; i++) {
					if (!StringUtils.isBlank(record.get(i))) {
						String columnName = StringUtils.substring(columns[i].trim(),
								(columns[i].trim().indexOf(".") + 1));
						String colmValue = record.get(i).trim();
						if (PMConstants.CONTACTS.equalsIgnoreCase(columnName)) {
							colmValue = getContactValue(colmValue);
						}
						map.put(columnName, colmValue);

					} else {
						String str = "Row:" + row + " Column :" + (i + 1) + " " + columns[i] + " value  is missing";
						lsterrors.add(str);
					}
				}
				lst.add(map);
			}

			dto.setLstMap(lst);
			dto.setCsvMap(getMap(lst));
			dto.setLstErrors(lsterrors);
			return dto;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}finally {
			if(fileReader!=null) {
			fileReader.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public CsvDto readExcel(String templateId, InputStream is) throws IOException {
		CsvDto dto = new CsvDto();
		HSSFWorkbook wb  = null;
		List<Map<Object, Object>> lst = new ArrayList<>();
		List<String> lsterrors = new ArrayList<>();
		int i =0;
		try {

			List<String> templVarLst = fetchTemplateHeader(templateId);
			dto.setTemplateId(templateId);

			// creating workbook instance that refers to .xls file
			 wb = new HSSFWorkbook(is);
			// creating a Sheet object to retrieve the object
			HSSFSheet sheet = wb.getSheetAt(0);

			// evaluating cell type
			FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
			for (Row row : sheet) // iteration over row using for each loop
			{	Map<Object, Object> map = new HashMap<>();
				String headerName=null;
				int j =0;
				for (Cell cell : row) // iteration over cell using for each loop
				{
					switch (formulaEvaluator.evaluateInCell(cell).getCellType()) {
					case Cell.CELL_TYPE_NUMERIC: // field that represents numeric cell type
						// getting the value of the cell as a number
						headerName = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_STRING: // field that represents string cell type
						// getting the value of the cell as a string
						headerName = cell.getStringCellValue();
						break;
					default:
						headerName =String.valueOf(cell.getStringCellValue());
						break;
					}
					if (i==0 && !templVarLst.contains(headerName)) {
						String colHeadErr = "The selected template does not have a header " +headerName;
						lsterrors.add(colHeadErr);
					}
					
					if(i!=0) {
						 String columnName =templVarLst.get(j);
						 columnName = StringUtils.substring(columnName.trim(),(columnName.indexOf(".") + 1));
						 if (PMConstants.CONTACTS.equalsIgnoreCase(columnName)) {
							 headerName = getContactValue(headerName);
							}
						 
						 map.put(columnName, headerName);
						//LOGGER.info("i "+i+"\t j :"+j+"\t Temp value :"+templVarLst.get(j)+"\t headerName :"+headerName);
					}
				 j++;
				}
				i++;
				if(map!=null && !map.isEmpty()) {
					lst.add(map);
				}
			}
			
			
			dto.setLstMap(lst);
			dto.setCsvMap(getMap(lst));
			dto.setLstErrors(lsterrors);
			//LOGGER.info("Json Util :"+JsonUtil.toJsonPrettyPrint(dto));
			return dto;
			
		} catch (IOException e) {
			LOGGER.info("readExcel exception "+e.getMessage());
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}finally {
			if(wb!=null) {
				wb.close();
			}
		}
		
	}
	
	
	/** read xlsx file **/
	
	
	
 	@SuppressWarnings("deprecation")
	public CsvDto readExcelXS(String templateId, InputStream is) throws IOException {
		CsvDto dto = new CsvDto();
		XSSFWorkbook wb  = null;
		List<Map<Object, Object>> lst = new ArrayList<>();
		List<String> lsterrors = new ArrayList<>();
		int i =0;
		try {

			List<String> templVarLst = fetchTemplateHeader(templateId);
			dto.setTemplateId(templateId);
			
			 wb = new XSSFWorkbook(is);
			XSSFSheet sheet = wb.getSheetAt(0); // creating a Sheet object to retrieve object
			Iterator<Row> itr = sheet.iterator(); // iterating over excel file
			
			while (itr.hasNext()) {
				Map<Object, Object> map = new HashMap<>();
				String headerName=null;
				int j =0;
				Row row = itr.next();
				Iterator<Cell> cellIterator = row.cellIterator(); // iterating over each column
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING: // field that represents string cell type
						headerName = cell.getStringCellValue();
						break;
					case Cell.CELL_TYPE_NUMERIC: // field that represents number cell type
						headerName =String.valueOf(cell.getNumericCellValue());
						break;
					default:
						headerName =String.valueOf(cell.getStringCellValue());
					}
					
					if(i!=0) {
						 String columnName =templVarLst.get(j);
						 columnName = StringUtils.substring(columnName.trim(),(columnName.indexOf(".") + 1));
						 if (PMConstants.CONTACTS.equalsIgnoreCase(columnName)) {
							 headerName = getContactValue(headerName);
							}
						 
						 map.put(columnName, headerName);
						//LOGGER.info("i "+i+"\t j :"+j+"\t Temp value :"+templVarLst.get(j)+"\t headerName :"+headerName);
					}
				 j++;
				}
				System.out.println("");
				i++;
				if(map!=null && !map.isEmpty()) {
					lst.add(map);
				}
			}
			
			
			dto.setLstMap(lst);
			dto.setCsvMap(getMap(lst));
			dto.setLstErrors(lsterrors);
			LOGGER.info("Json Util :"+JsonUtil.toJsonPrettyPrint(dto));
			return dto;
			
		} catch (IOException e) {
			LOGGER.info("readExcel exception "+e.getMessage());
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}finally {
			if(wb!=null) {
				wb.close();
			}
		}
		
	}
	
	

	public List<String> fetchTemplateHeader(String templateId) {
		Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
		List<String> templVarLst = new ArrayList<>();
		templVarLst.add(PMConstants.CONTACTS);
		HSMTemplateDoc templateDoc = mongoTemplate.findById(templateId, HSMTemplateDoc.class);
		if (templateDoc != null) {
			String template = templateDoc.getTemplate();
			Matcher matcher = pattern.matcher(template);
			while (matcher.find()) {
				String headerName = matcher.group(1);
				templVarLst.add(headerName);
			}
		}
		LOGGER.info("fetchTemplateHeader :" + JsonUtil.toJsonPrettyPrint(templVarLst));

		return templVarLst;
	}

	private String getContactValue(String colmValue) {
		int compare = Character.compare(colmValue.charAt(0), '+');
		if (compare != 0) {
			colmValue = PMConstants.PLUS_SYM.concat(colmValue);
		}
		return colmValue;
	}
	
	private Map<Object, List<Object>> getMap(List<Map<Object, Object>> lst){
		Map<Object, List<Object>> combined = null;
		if (lst != null && !lst.isEmpty()) {
			combined = lst.stream().flatMap(m -> m.entrySet().stream()).collect(
					Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
			
		}
		return combined;
	}

}
