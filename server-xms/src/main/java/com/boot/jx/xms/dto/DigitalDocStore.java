package com.boot.jx.xms.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonDocStore;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

@Component
public class DigitalDocStore extends CommonDocStore {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	public static String getCollectionName(String ddoType) {
		return (DigitalDataDoc.COLLECTION_NAME + "_" + ArgUtil.parseAsString(ddoType, "OTHERS"));
	}

	public DigitalDataDoc createAndUpdateDigitalDoc(DigitalObjectDto digitalObjectDto) {
		DigitalDataDoc digitalDataDoc = new DigitalDataDoc();
		if (ArgUtil.is(digitalObjectDto.getId())) {
			digitalDataDoc = findDigitalDocById(digitalObjectDto);
			if (ArgUtil.is(digitalDataDoc)) {
				digitalDataDoc.setUpdatedStamp(System.currentTimeMillis());
				digitalDataDoc.setId(digitalObjectDto.getId());
				digitalDataDoc.setType(digitalObjectDto.getType());
				digitalDataDoc.setLinks(digitalObjectDto.getLinks());
				digitalDataDoc.setData(digitalObjectDto.getData());
			} else {
				digitalDataDoc = createDigitalDoc(digitalObjectDto);
			}
		}
		mongoTemplate.save(digitalDataDoc, getCollectionName(digitalObjectDto.getType()));
		return digitalDataDoc;

	}

	private DigitalDataDoc findDigitalDocById(DigitalObjectDto digitalObjectDto) {
		DigitalDataDoc digitalDoc = null;
		if (ArgUtil.is(digitalObjectDto.getId())) {
			String id = digitalObjectDto.getId();
			digitalDoc = mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), DigitalDataDoc.class,
					getCollectionName(digitalObjectDto.getType()));
		}
		return digitalDoc;
	}

	private DigitalDataDoc createDigitalDoc(DigitalObjectDto digitalObjectDto) {
		DigitalDataDoc digitalDataDoc = new DigitalDataDoc();
		digitalDataDoc.setId(digitalObjectDto.getId());
		digitalDataDoc.setType(digitalObjectDto.getType());
		digitalDataDoc.setLinks(digitalObjectDto.getLinks());
		digitalDataDoc.setData(digitalObjectDto.getData());
		digitalDataDoc.setCreatedStamp(System.currentTimeMillis());
		digitalDataDoc.setUpdatedStamp(null);
		return digitalDataDoc;
	}

	/** to save digital event **/

	public void insertDigitalEvent(DigitalEventDto digitalEventDto) {
		DigitalEvent digitalEvent = EntityDtoUtil.dtoToEntity(digitalEventDto, new DigitalEvent());
		digitalEvent.setCreatedStamp(System.currentTimeMillis());
		mongoTemplate.save(digitalEvent);
	}

}
