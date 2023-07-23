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

	List<OtpAlertEvent> findByCompanyIdAndCreatedHourGreaterThan(final String companyId, final Long startCreatedHour);

	List<OtpAlertEvent> findByCompanyIdAndCreatedHourGreaterThan(final String companyId, final Long startCreatedHour,
			final Pageable pageRequest);

	List<OtpAlertEvent> findByCompanyIdAndCreatedHourLessThan(final String companyId, final Long endCreatedHour);

	List<OtpAlertEvent> findByCompanyIdAndCreatedHourBetween(final String companyId, final Long startCreatedHour,
			final Long endCreatedHour);

	List<OtpAlertEvent> findByCompanyIdAndCreatedHourBetween(final String companyId, final Long startCreatedHour,
			final Long endCreatedHour, final Pageable pageRequest);

}
