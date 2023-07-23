package com.boot.jx.contak.cache;

import java.util.List;
import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface OtpAlertEventRepository extends CrudRepository<OtpAlertEvent, OtpAlertEventId> {

	Optional<OtpAlertEvent> findByEventId(String eventId);

	List<OtpAlertEvent> findByCompanyId(String companyId);

	List<OtpAlertEvent> findByCompanyQueueAndCreatedHourGreaterThan(final String companyQueue,
			final Long startCreatedHour);

	List<OtpAlertEvent> findByCompanyQueueAndCreatedHourGreaterThan(final String companyQueue,
			final Long startCreatedHour, final Pageable pageRequest);

	List<OtpAlertEvent> findByCompanyQueueAndCreatedHourLessThan(final String companyQueue, final Long endCreatedHour);

	List<OtpAlertEvent> findByCompanyQueueAndCreatedHourBetween(final String companyQueue, final Long startCreatedHour,
			final Long endCreatedHour);

	List<OtpAlertEvent> findByCompanyQueueAndCreatedHourBetween(final String companyQueue, final Long startCreatedHour,
			final Long endCreatedHour, final Pageable pageRequest);

}
