package com.boot.jx.postman.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.utils.CryptoUtil.HashBuilder;

@Component
public class DocumentService {

	
	@Value("${jax.doc.url}")
	private String dcoUrl;

	@Value("${jax.doc.key}")
	private String dcoKey;

	public String generateUrl() {
		String traceId = AppContextUtil.getTraceId();
		long timestamp = System.currentTimeMillis();
		return String.format("%s/upload/%d/%s/%s", dcoUrl, timestamp, traceId,
				new HashBuilder().secret(dcoKey).message(traceId).toHMAC().output());
	}
	
}
