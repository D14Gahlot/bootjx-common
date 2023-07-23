package com.boot.jx.contak.cache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

@Component
public class OtpAlertEventManager {

	@Autowired
	private OtpAlertEventRepository otpAlertEventRepository;

	@Autowired
	private OtpAlertNotifiedEventRepository otpAlertNotifiedEventRepository;

	public List<OtpAlertEvent> listAllOtpAlertEvents() {
		Iterable<OtpAlertEvent> otpAlertEvents = otpAlertEventRepository.findAll();
		return Lists.newArrayList(otpAlertEvents);
	}

	public OtpAlertEvent createOtpAlertEvent(final UUID eventId, final Long createdHour, final String companyId,
			final String phoneId) {
		OtpAlertEventId otpAlertEventId = new OtpAlertEventId(eventId.toString(), createdHour);
		return otpAlertEventRepository.save(new OtpAlertEvent(otpAlertEventId, companyId, phoneId));
	}

	public OtpAlertEvent getOtpAlertEvent(final String eventId) {
		Optional<OtpAlertEvent> event = otpAlertEventRepository.findByEventId(eventId);
		if (event.isPresent()) {
			return event.get();
		}
		return null;
	}

//	public List<OtpAlertEvent> listOtpAlertEvents() {
//		Iterable<OtpAlertEvent> otpAlertEvents = otpAlertEventRepository.findByCompanyIdAndCreatedHourGreaterThan(null, null)
//		return Lists.newArrayList(otpAlertEvents);
//	}
}
