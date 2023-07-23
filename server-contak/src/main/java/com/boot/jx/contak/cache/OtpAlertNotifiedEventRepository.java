package com.boot.jx.contak.cache;

import java.util.List;
import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface OtpAlertNotifiedEventRepository extends CrudRepository<OtpAlertNotifiedEvent, OtpAlertEventId> {

	Optional<OtpAlertNotifiedEvent> findByEventId(String eventId);

	List<OtpAlertNotifiedEvent> findByCompanyId(String companyId);

	List<OtpAlertNotifiedEvent> findByCompanyIdAndCreatedHourGreaterThan(final String companyId,
			final Long startCreatedHour);

	List<OtpAlertNotifiedEvent> findByCompanyIdAndCreatedHourGreaterThan(final String companyId,
			final Long startCreatedHour, final Pageable pageRequest);

	List<OtpAlertNotifiedEvent> findByCompanyIdAndCreatedHourLessThan(final String companyId,
			final Long endCreatedHour);

	List<OtpAlertNotifiedEvent> findByCompanyIdAndCreatedHourBetween(final String companyId,
			final Long startCreatedHour, final Long endCreatedHour);

	List<OtpAlertNotifiedEvent> findByCompanyIdAndCreatedHourBetween(final String companyId,
			final Long startCreatedHour, final Long endCreatedHour, final Pageable pageRequest);

}
