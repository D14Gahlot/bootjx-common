package com.boot.jx.common.helper;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBWork;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.UniqueID;

@Component
public class ExcelHelper {

	public List<Map<String, Object>> convertExcelToFormattedString() throws Exception {
		Workbook workbook = null;
		FileInputStream file = null;
		List<Map<String, Object>> lstMaps = new ArrayList<>();
		try {

			String fileP = "D:\\Project\\M-Y\\M-Y\\customer_profile\\Annexure2_xlsx_new.xlsx";
			file = new FileInputStream(fileP);
			workbook = WorkbookFactory.create(file);
			Sheet sheet = workbook.getSheetAt(0); // Assuming the first sheet
			StringBuilder formattedString = new StringBuilder();

			// Iterate through each row starting from row 2
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row headerRow = sheet.getRow(0);
				Row dataRow = sheet.getRow(rowIndex);
				Map<String, Object> map = new HashMap<>();
				if (headerRow != null && dataRow != null) {
					// Iterate through each cell in the row
					for (int i = 0; i < headerRow.getLastCellNum(); i++) {
						Cell headerCell = headerRow.getCell(i);
						Cell dataCell = dataRow.getCell(i);
						if (headerCell != null) {
							String header = headerCell.getStringCellValue();
							String value = null;
							if (dataCell != null) {
								value = dataCell.getStringCellValue();
							}

							formattedString.append(header).append(": ").append(value).append(", ");
							map.put(header, value);
						}
						// lstMaps.add(map);
					}
					// Add a new line after each row
					formattedString.append("\n");
					// sorting maps
					if (map != null && !map.isEmpty()) {
						map = map.entrySet().stream().sorted(Map.Entry.comparingByKey()).collect(LinkedHashMap::new,
								(result, entry) -> result.put(entry.getKey(), entry.getValue()), Map::putAll);
					}

					lstMaps.add(map);
				}
			}
			if (formattedString.length() > 0) {
				formattedString.delete(formattedString.length() - 2, formattedString.length());
			}
			System.out.println("lstMaps :" + formattedString.toString());
			return lstMaps;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workbook.close();
			file.close();
		}
		return lstMaps;
	}

	public List<CustomerProfileDoc> createCustomerProfile() throws Exception {
		List<CustomerProfileDoc> lstCustProfiDocs = new ArrayList<>();

		try {
			List<Map<String, Object>> lstMaps = convertExcelToFormattedString();

			for (Map<String, Object> map : lstMaps) {
				CustomerProfileDoc cProfileDoc = new CustomerProfileDoc();
				PBName pbName = new PBName();
				Set<PBEmail> setPbEmail = new TreeSet<PBEmail>();
				Set<PBPhone> setPbPhone = new TreeSet<PBPhone>();
				Set<PBWork> setPbWork = new TreeSet<PBWork>();

				Map<String, Object> additionalInfoMap = new HashMap<String, Object>();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String key = entry.getKey().toLowerCase().trim();
					Object value = entry.getValue();
					if (key != null && value != null) {
						if (key.equalsIgnoreCase("name") || key.equalsIgnoreCase("firtst name")
								|| key.equalsIgnoreCase("last name")) {
							checkName(key, value, pbName);
						} else if (key.contains("mail")) {
							PBEmail pbEmail = checkEmail(key, value);
							setPbEmail.add(pbEmail);
						} else if (key.contains("phone")) {
							PBPhone pbPhone = checkPhone(key, value);
							setPbPhone.add(pbPhone);
						} else if (key.contains("organization")) {
							// PBWork pbWork = checkWorkOrganisation(key, value);
							// setPbWork.add(pbWork);
						}
					} else {
						additionalInfoMap.put(key, value);
					}
				}
				cProfileDoc.setName(pbName);
				cProfileDoc.setEmails(setPbEmail);
				cProfileDoc.setPhones(setPbPhone);
				cProfileDoc.setWorks(setPbWork);
				cProfileDoc.setAdditionalInfo(additionalInfoMap);
				lstCustProfiDocs.add(cProfileDoc);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lstCustProfiDocs;

	}

	private static void checkName(String key, Object value, PBName pbName) {
		switch (key) {
		case "first name":
		case "firstname":
			pbName.setFirstName(ArgUtil.parseAsString(value, Constants.BLANK));
		case "last name":
		case "lastname":
			pbName.setLastName(ArgUtil.parseAsString(value, Constants.BLANK));
		case "middle name":
		case "middlename":
			pbName.setMiddleName(ArgUtil.parseAsString(value, Constants.BLANK));
		default:
			pbName.setFormattedName(ArgUtil.parseAsString(value, Constants.BLANK));
		}

	}

	private static PBEmail checkEmail(String key, Object value) {
		PBEmail pbEmail = new PBEmail();
		if (key.contains("type")) {
			pbEmail.setUuid(UniqueID.generateString());
			pbEmail.setLabel(key);
			pbEmail.setType(ArgUtil.parseAsString(value, Constants.BLANK));
			pbEmail.setEmail(ArgUtil.parseAsString(null, Constants.BLANK));
			return pbEmail;
		} else if (key.contains("value")) {
			pbEmail.setUuid(UniqueID.generateString());
			pbEmail.setLabel(key);
			pbEmail.setEmail(value.toString());
			pbEmail.setType(ArgUtil.parseAsString(null, Constants.BLANK));
			return pbEmail;
		}

		return null;
	}

	private static PBPhone checkPhone(String key, Object value) {
		PBPhone pbPhone = new PBPhone();
		if (key.contains("type")) {
			pbPhone.setUuid(UniqueID.generateString());
			pbPhone.setLabel(key);
			pbPhone.setType(ArgUtil.parseAsString(value, Constants.BLANK));
			pbPhone.setPhone(ArgUtil.parseAsString(null, Constants.BLANK));
			pbPhone.setNationalNumber(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbPhone;
		} else if (key.contains("value")) {
			pbPhone.setUuid(UniqueID.generateString());
			pbPhone.setLabel(key);
			pbPhone.setPhone(ArgUtil.parseAsString(value, Constants.BLANK));
			pbPhone.setType(ArgUtil.parseAsString(null, Constants.BLANK));
			pbPhone.setNationalNumber(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbPhone;
		}

		return null;
	}

	private static PBWork checkWorkOrganisation(String key, Object value) {
		PBWork pbWork = new PBWork();
		if (key.contains("type")) {
			pbWork.setLabel(key);
			pbWork.setTitle(ArgUtil.parseAsString(value, Constants.BLANK));
		} else if (key.contains("title")) {
			pbWork.setLabel(key);
			pbWork.setTitle(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbWork;
		} else if (key.contains("name")) {
			pbWork.setLabel(key);
			pbWork.setDepartment(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbWork;
		} else if (key.contains("department")) {
			pbWork.setLabel(key);
			pbWork.setTitle(ArgUtil.parseAsString(value, Constants.BLANK));
			pbWork.setDepartment(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbWork;
		} else if (key.contains("symbol")) {
			pbWork.setLabel(key);
			pbWork.setTitle(ArgUtil.parseAsString(value, Constants.BLANK));
			pbWork.setDepartment(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbWork;
		} else if (key.contains("location")) {
			pbWork.setLabel(key);
			pbWork.setTitle(ArgUtil.parseAsString(value, Constants.BLANK));
			pbWork.setDepartment(ArgUtil.parseAsString(value, Constants.BLANK));
			return pbWork;
		}
		return null;
	}

}
