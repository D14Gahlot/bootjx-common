package com.boot.jx.contak.cache;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.utils.EntityDtoUtil;

@Component
public class WebhookRespManager {

	@Autowired
	private WebhookResponseHolderRepo webhookResponseHolderRepo;

	public void save(WebhookRespHolder dto) {
		WebhookRespHolderDoc doc = EntityDtoUtil.dtoToEntity(dto, new WebhookRespHolderDoc());
		webhookResponseHolderRepo.save(doc);
	}

	public Optional<WebhookRespHolder> findById(String nonce) {
		Optional<WebhookRespHolderDoc> x = webhookResponseHolderRepo.findById(nonce);
		if (x.isPresent()) {
			return Optional.of(x.get());
		}
		return Optional.empty();
	}

	public void delete(WebhookRespHolder resp) {
		webhookResponseHolderRepo.deleteById(resp.getId());
	}

}
