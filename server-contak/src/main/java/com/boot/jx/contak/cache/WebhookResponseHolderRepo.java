package com.boot.jx.contak.cache;

import java.util.Optional;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.repository.CrudRepository;

@EnableScan
public interface WebhookResponseHolderRepo extends CrudRepository<WebhookRespHolderDoc, String> {

	Optional<WebhookRespHolderDoc> findById(String id);
}
