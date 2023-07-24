package com.boot.jx.contak.cache;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.contak.cache.OtpAlertEvent.CompanyQueueStatus;
import com.boot.jx.contak.cache.OtpAlertEvent.MessageEventDynmo;
import com.boot.jx.contak.cache.OtpAlertEvent.TimeStampIndexDynmo;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.doc.ContakMessageTrace;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.dto.ContakInboundDoc;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.contak.manager.ContakInboundManager.USER_INBOUND_TYPE;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.JsonUtil;
import com.google.common.collect.Lists;

@Component
public class OtpAlertEventManager {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private OtpAlertEventRepository otpAlertEventRepository;

	public List<OtpAlertEvent> listAllOtpAlertEvents() {
		Iterable<OtpAlertEvent> otpAlertEvents = otpAlertEventRepository.findAll();
		return Lists.newArrayList(otpAlertEvents);
	}

	public OtpAlertEvent createOtpAlertEvent(final String eventId, final String companyId, final String phoneId) {
		TimeStampIndex created = TimeStampIndex.from(System.currentTimeMillis());
		OtpAlertEventId otpAlertEventId = new OtpAlertEventId(eventId, created.getHour());
		return otpAlertEventRepository
				.save(new OtpAlertEvent(otpAlertEventId, companyId, phoneId).update(CompanyQueueStatus.CRTD));
	}

	public OtpAlertEvent getOtpAlertEvent(final String eventId) {
		Optional<OtpAlertEvent> event = otpAlertEventRepository.findByEventId(eventId);
		if (event.isPresent()) {
			return event.get();
		}
		return null;
	}

	public List<OtpAlertEvent> pollOtpAlertEvents(String companyId) {
		TimeStampIndex created = TimeStampIndex.from(System.currentTimeMillis());

		OtpAlertEvent otpAlertEventQuery = new OtpAlertEvent(null, companyId, null).update(CompanyQueueStatus.CRTD);

		Iterable<OtpAlertEvent> otpAlertEvents = otpAlertEventRepository.findByCompanyQueueAndCreatedHourGreaterThan(
				otpAlertEventQuery.getCompanyQueue(), created.getHour() - 1, PageRequest.of(0, 5));

		for (OtpAlertEvent otpAlertEvent : otpAlertEvents) {
			otpAlertEvent.update(CompanyQueueStatus.NTFD);
		}

		otpAlertEventRepository.saveAll(otpAlertEvents);

		return Lists.newArrayList(otpAlertEvents);
	}

	public void sendHandShakeAckEvent(UserRegistrationDoc userRegistrationDoc) {
		OtpAlertEvent inbound = new OtpAlertEvent();
		inbound.setInboundType("HANDSHAKE_ACK"); // Earlier it was USER_REG
		inbound.setPhoneId(userRegistrationDoc.getUserPhoneNumber());
		inbound.setCompanyId(userRegistrationDoc.getCompanyId());
		inbound.setCreatedAt(TimeStampIndexDynmo.from(userRegistrationDoc.getCreatedAt()));
		inbound.setNotifiedAt(TimeStampIndexDynmo.from(userRegistrationDoc.getDeliveredAt()));
		inbound.setExpiredAt(TimeStampIndexDynmo.from(userRegistrationDoc.getExpiredAt()));
		inbound.setInboundPayload(JsonUtil.toJsonMap(userRegistrationDoc));
		otpAlertEventRepository.save(inbound);
	}

	public void sendMsgDelvryEvent(ContakMessageDoc contakMessageDoc) {
		OtpAlertEvent inbound = new OtpAlertEvent();
		inbound.setInboundType(USER_INBOUND_TYPE.MSG_OUT_DELIVERED);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setCreatedAt(TimeStampIndexDynmo.now());
		inbound.setInboundPayload(
				JsonUtil.toJsonMap(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace())));
		otpAlertEventRepository.save(inbound);
	}

	@Async
	public void sendMsgDelvryEventAsync(List<ContakMessageDoc> messages) {
		for (ContakMessageDoc contakMessageDoc : messages) {
			this.sendMsgDelvryEvent(contakMessageDoc);
		}
	}

	public void sendMsgReadEvent(ContakMessageDoc contakMessageDoc) {
		OtpAlertEvent inbound = new OtpAlertEvent();
		inbound.setInboundType(USER_INBOUND_TYPE.MSG_OUT_READ);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setCreatedAt(TimeStampIndexDynmo.now());
		inbound.setInboundPayload(
				JsonUtil.toJsonMap(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace())));
		otpAlertEventRepository.save(inbound);
	}

	@Async
	public void sendMsgReadEventAsync(List<ContakMessageDoc> messages) {
		for (ContakMessageDoc contakMessageDoc : messages) {
			this.sendMsgReadEvent(contakMessageDoc);
		}
	}

	public void sendUserAuthEvent(PhoneUserDoc phoneUserDoc, String isUserRegistraion) {
		CompanyDoc comp = commonMongoTemplate
				.findOne(MQB.select(CompanyDoc.class).where(QueryCriteria.where("clientId").is("mehery")));
		if (ArgUtil.is(comp)) {
			OtpAlertEvent inbound = new OtpAlertEvent();
			inbound.setInboundType(isUserRegistraion);
			inbound.setPhoneId(phoneUserDoc.getPhoneId());
			inbound.setCompanyId(comp.getCompanyId());
			inbound.setCreatedAt(TimeStampIndexDynmo.now());
			inbound.setInboundPayload(JsonUtil.toJsonMap(new ContakMessageTrace()));
			otpAlertEventRepository.save(inbound);
		}
	}

	@Async
	public void sendMsgLogEventAsync(ContakMessageDoc contakMessageDoc, MessageEvent event) {
		OtpAlertEvent inbound = new OtpAlertEvent();
		inbound.setInboundType(USER_INBOUND_TYPE.MSG_OUT_LOG);
		inbound.setPhoneId(contakMessageDoc.getPhoneId());
		inbound.setCompanyId(contakMessageDoc.getCompanyId());
		inbound.setCreatedAt(TimeStampIndexDynmo.now());
		inbound.setInboundPayload(
				JsonUtil.toJsonMap(EntityDtoUtil.entityToDto(contakMessageDoc, new ContakMessageTrace())));
		inbound.setEvent(EntityDtoUtil.entityToDto(event, new MessageEventDynmo()));
		otpAlertEventRepository.save(inbound);
	}
}
