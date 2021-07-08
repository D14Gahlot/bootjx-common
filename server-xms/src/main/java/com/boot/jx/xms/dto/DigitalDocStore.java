package com.boot.jx.xms.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocStore;
import com.boot.utils.ArgUtil;

@Component
public class DigitalDocStore  extends CommonDocStore {
	
	@Autowired
	MongoTemplate mongoTemplate;

	public static String getCollectionName(String ddoType) {
		return (DigitalDataDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(ddoType, "OTHERS"));
	}
	
	public DigitalDataDoc createAndUpdateDigitalDoc(DigitalObjectDto digitalObjectDto) {
		DigitalDataDoc digitalDataDoc= new DigitalDataDoc();
		if(ArgUtil.is(digitalObjectDto.getId())) {
			digitalDataDoc = findDigitalDocById(digitalObjectDto);
			if(ArgUtil.is(digitalDataDoc)) {
				digitalDataDoc.setUpdatedStamp(System.currentTimeMillis());
				digitalDataDoc.setId(digitalObjectDto.getId());
				digitalDataDoc.setType(digitalObjectDto.getType());
				digitalDataDoc.setLinks(digitalObjectDto.getLinks());
				digitalDataDoc.setData(digitalObjectDto.getData());
			}else {
				digitalDataDoc =createDigitalDoc(digitalObjectDto);
			}
		}
		mongoTemplate.save(digitalDataDoc,getCollectionName(digitalObjectDto.getType()));
		return digitalDataDoc;
		
	}
	
	private DigitalDataDoc findDigitalDocById(DigitalObjectDto digitalObjectDto) {
		if(ArgUtil.is(digitalObjectDto.getId())) {
			return mongoTemplate.findById(digitalObjectDto.getId(), DigitalDataDoc.class);
		}
		
		return null;
	}
	
	
	private DigitalDataDoc createDigitalDoc(DigitalObjectDto digitalObjectDto) {
		DigitalDataDoc digitalDataDoc= new DigitalDataDoc();
		digitalDataDoc.setId(digitalObjectDto.getId());
		digitalDataDoc.setType(digitalObjectDto.getType());
		digitalDataDoc.setLinks(digitalObjectDto.getLinks());
		digitalDataDoc.setData(digitalObjectDto.getData());
		digitalDataDoc.setCreatedStamp(System.currentTimeMillis());
		digitalDataDoc.setUpdatedStamp(null);
		return digitalDataDoc;
	}
	
	
	
}	
