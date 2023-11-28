package com.boot.jx.contak.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.doc.ContakMessageTrace;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.dto.ContakInboundDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

@Component
public class ContakInboundManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContakInboundManager.class);

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public List<ContakInboundDoc> fetchInbounds(String companyId) {
		TimeStampIndex notifiedAt = TimeStampIndex.from(System.currentTimeMillis());
		commonMongoTemplate.update(CommonMongoQueryBuilder.collection(ContakInboundDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId)
						// only if it has not been notified yet
						.and("notifiedAt").exists(false)
						// Consider expiry of inbound only if it was defined at the time of creation
						.orOperator(Criteria.where("expiredAt.hour").gte(notifiedAt.getHour() - 1),
								Criteria.where("expiredAt").exists(false)))
				// Update
				.set("notifiedAt", notifiedAt));

		return commonMongoTemplate.find(CommonMongoQueryBuilder.collection(ContakInboundDoc.class).where( // FIND
				CommonMongoQueryBuilder.QueryCriteria.where("companyId").is(companyId).and("notifiedAt.stamp")
						.is(notifiedAt.getStamp())));
	}

	public void sendHandShakeAckEvent(UserRegistrationDoc userRegistrationDoc) {
		ContakInboundDoc inbound = ContakInboundDoc.create(ContakInboundRouter.USER_INBOUND_TYPE.HANDSHAKE_ACK);
		inbound.setPhoneId(userRegistrationDoc.getUserPhoneNumber());
		inbound.setCompanyId(userRegistrationDoc.getCompanyId());
		inbound.setCreatedAt(userRegistrationDoc.getCreatedAt());
		inbound.setNotifiedAt(userRegistrationDoc.getDeliveredAt());
		inbound.setExpiredAt(userRegistrationDoc.getExpiredAt());
		inbound.setInboundPayload(userRegistrationDoc);
		commonMongoTemplate.save(inbound);
	}

	public void sendMsgDelvryEvent(ContakMessageDoc contakMessageDoc) {
		ContakInboundDoc inbound = ContakInboundDoc.create(ContakInboundRouter.USER_INBOUND_TYPE.MSG_OUT_DELIVERED);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setInboundPayload(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace()));
		commonMongoTemplate.save(inbound);
	}

	@Async
	public void sendMsgDelvryEventAsync(List<ContakMessageDoc> messages) {
		for (ContakMessageDoc contakMessageDoc : messages) {
			this.sendMsgDelvryEvent(contakMessageDoc);
		}
	}

	public void sendMsgReadEvent(ContakMessageDoc contakMessageDoc) {
		ContakInboundDoc inbound = ContakInboundDoc.create(ContakInboundRouter.USER_INBOUND_TYPE.MSG_OUT_READ);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setInboundPayload(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace()));
		commonMongoTemplate.save(inbound);
	}

	@Async
	public void sendMsgReadEventAsync(List<ContakMessageDoc> messages) {
		for (ContakMessageDoc contakMessageDoc : messages) {
			this.sendMsgReadEvent(contakMessageDoc);
		}
	}

	public void sendUserAuthEvent(PhoneUserDoc phoneUserDoc, String inBoundType) {
		CompanyDoc comp = commonMongoTemplate
				.findOne(MQB.select(CompanyDoc.class).where(QueryCriteria.where("clientId").is("mehery")));
		if (ArgUtil.is(comp)) {
			ContakInboundDoc inbound = ContakInboundDoc.create(inBoundType);
			inbound.setPhoneId(phoneUserDoc.getPhoneId());
			inbound.setCompanyId(comp.getCompanyId());
			inbound.setInboundPayload(new ContakMessageTrace());
			commonMongoTemplate.save(inbound);
		}
	}

	@Async
	public void sendMsgLogEventAsync(ContakMessageDoc contakMessageDoc, MessageEvent event) {
		ContakInboundDoc inbound = ContakInboundDoc.create(ContakInboundRouter.USER_INBOUND_TYPE.MSG_OUT_LOG);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setInboundPayload(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace()));
		inbound.setEvent(event);
		commonMongoTemplate.save(inbound);
	}

}
