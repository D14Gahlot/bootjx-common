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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.CsvDto;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.FILE_TYPE;
import com.boot.jx.postman.doc.HSMTemplateDoc;

@Component
public class CSVHelper {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	public static String TYPE = "text/csv";
	
	public static String tYPE_EXCEL="application/vnd.ms-excel";

	public static boolean hasCSVFormat(MultipartFile file) {
		if (!FILE_TYPE.CSV.equals(file.getContentType())) {
			return false;
		}
		return true;
	}
	
	
	public static boolean hasExcelFormat(MultipartFile file) {
		if (!FILE_TYPE.EXCEL.equals(file.getContentType())) {
			return false;
		}
		return true;
	}


	public CsvDto csvToTutorials(String templateId, InputStream is) {
		CsvDto dto = new CsvDto();
		List<Map<Object, Object>> lst = new ArrayList<>();
		List<String> lsterrors = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
		List<String> templVarLst = new ArrayList<>();
		templVarLst.add(PMConstants.CONTACTS);
		dto.setTemplateId(templateId);	
		HSMTemplateDoc templateDoc = mongoTemplate.findById(templateId, HSMTemplateDoc.class);
		if (templateDoc != null) {
			String template = templateDoc.getTemplate();
			Matcher matcher = pattern.matcher(template);
			while (matcher.find()) {
				String headerName = matcher.group(1);
				//if (!headerName.contains("contact")) {
					//templVarLst.add(StringUtils.substring(headerName, (headerName.indexOf(".") + 1)));
					templVarLst.add(headerName);
					//}

			}
		}
		try {
			BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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
						String columnName = StringUtils.substring(columns[i].trim(), (columns[i].trim().indexOf(".") + 1));
						String colmValue=record.get(i).trim();
						if(PMConstants.CONTACTS.equalsIgnoreCase(columnName)) {
							colmValue = getContactValue(colmValue);
						}
						map.put(columnName,colmValue);
						
					} else {
						String str = "Row:" + row + " Column :" + (i + 1) + " " + columns[i] + " value  is missing";
						lsterrors.add(str);
					}
				}
				lst.add(map);
			}

			dto.setLstMap(lst);

			if (lst != null && !lst.isEmpty()) {
				Map<Object, List<Object>> combined = lst.stream().flatMap(m -> m.entrySet().stream()).collect(
						Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList())));
				dto.setCsvMap(combined);
			}
			dto.setLstErrors(lsterrors);
			return dto;
		} catch (IOException e) {
			throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
		}
	}
	
	private String getContactValue(String colmValue) {
		int compare = Character.compare(colmValue.charAt(0), '+');
		if(compare!=0) {
			colmValue =PMConstants.PLUS_SYM.concat(colmValue);
		}
		return colmValue;
	}
	
}
