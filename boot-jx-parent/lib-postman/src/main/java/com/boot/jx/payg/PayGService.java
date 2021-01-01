package com.boot.jx.payg;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.apache.log4j.Logger;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConstants;
import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.PayGServiceCode;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.payg.PayGParams.PayGConstants;
import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RequestMetaInfo.CommonRequestMetaInfo;
import com.boot.jx.scope.TenantContextHolder;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.URLBuilder;
import com.boot.utils.Urly;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PayGService {

	private static final Logger LOGGER = Logger.getLogger(PayGService.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private RestService restService;

	//@Autowired
	//PayGMetaInfo payGMetaInfo;

	private static BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
	{
		textEncryptor.setPasswordCharArray("ZNEAYuVTsC".toCharArray());
	}

	public String getPaymentUrl(Payment payment, String callback) throws MalformedURLException, URISyntaxException {
		AppContext context = AppContextUtil.getContext();

		URLBuilder builder = new URLBuilder(appConfig.getPaygURL());

		String callbackUrl = callback
				+ "?docNo=" + payment.getDocNo()
				+ "&docFy=" + payment.getDocFinYear()
				+ "&docId=" + payment.getDocId()
				+ "&trckid=" + payment.getMerchantTrackId();
		String callbackd = Base64.getEncoder().encodeToString(callbackUrl.getBytes());

		builder.path("app/payment").queryParam("amount", payment.getNetPayableAmount())
				.queryParam("trckid", payment.getMerchantTrackId()).queryParam("pg", payment.getPgCode())
				.queryParam("docFy", payment.getDocFinYear()).queryParam("docNo", payment.getDocNo())
				.queryParam("docId", payment.getDocId())
				.queryParam("tnt", context.getTenant()).queryParam("callbackd", callbackd)
				.queryParam("prod", payment.getProduct())
				.queryParam(AppConstants.TRACE_ID_XKEY, context.getTraceId());
		return builder.getURL();
	}

	public String getPaymentUrl(PayGParams payment, String callback, String fallbackUrl)
			throws MalformedURLException, URISyntaxException {
		
		if (ArgUtil.isEqual(payment.getServiceCode(), PayGServiceCode.CASH,PayGServiceCode.IPOS)) {
			payment = setVerifyHash(payment);
			return null;
		} 
		
		AppContext context = AppContextUtil.getContext();

		URLBuilder builder = new URLBuilder(appConfig.getPaygURL());

		if (PayGServiceCode.WT.equals(payment.getServiceCode())
				|| PayGServiceCode.PB.equals(payment.getServiceCode())) {
			return fallbackUrl;
		}

		/*
		 * if (PayGServiceCode.PB.equals(payment.getServiceCode())) { return
		 * fallbackUrl; }
		 */
		String callbackUrl = Urly.parse(callback)
				.queryParam("docNo", payment.getDocNo())
				.queryParam("docFy", payment.getDocFy())
				.queryParam("docId", payment.getDocId())
				.queryParam("trckid", payment.getTrackId())
				.queryParam("payId", payment.getPayId()).getURL();

		/*
		 * if (PayGServiceCode.WT.equals(payment.getServiceCode())) { return
		 * callbackUrl; }
		 */

		String callbackd = Base64.getEncoder().encodeToString(callbackUrl.getBytes());

		builder.path("app/payment").queryParam("amount", payment.getAmount())
				.queryParam("trckid", payment.getTrackId()).queryParam("pg", payment.getServiceCode())
				.queryParam("docFy", payment.getDocFy()).queryParam("docNo", payment.getDocNo())
				.queryParam("docId", payment.getDocId())
				.queryParam("tnt", context.getTenant()).queryParam("callbackd", callbackd)
				.queryParam("prod", payment.getProduct())
				.queryParam("payId", payment.getPayId())
				.queryParam("channel", context.getClient().getChannel())
				.queryParam(AppConstants.DEVICE_ID_XKEY, context.getClient().getFingerprint())
				.queryParam(AppConstants.UDC_CLIENT_TYPE_XKEY, context.getClient().getClientType())
				.queryParam(AppConstants.TRACE_ID_XKEY, context.getTraceId())
				.queryParam("detail", getEnCryptedDetails(payment.getTrackId(), payment.getAmount(),
						payment.getDocId(), payment.getDocNo(), payment.getDocFy(), payment.getPayId()))
				.queryParam("verify", getVerifyHash(payment.getTrackId(), payment.getAmount(),
						payment.getDocId(), payment.getDocNo(), payment.getDocFy(), payment.getPayId())
								.getVerification());

		return builder.getURL();
	}

	public String getEnCryptedDetails(String trckid, String amount, String docId, String docNo,
			String docFy, String payId) {
		PayGParams payGParams = new PayGParams();
		payGParams.setAmount(amount);
		payGParams.setDocId(docId);
		payGParams.setDocNo(docNo);
		payGParams.setDocFy(docFy);
		payGParams.setTrackId(trckid);
		payGParams.setPayId(payId);
		return getEnCryptedDetails(payGParams);
	}

	@Deprecated
	public String getEnCryptedDetails(String trckid, String amount, String docId, String docNo,
			String docFy) {
		PayGParams payGParams = new PayGParams();
		payGParams.setAmount(amount);
		payGParams.setDocId(docId);
		payGParams.setDocNo(docNo);
		payGParams.setDocFy(docFy);
		payGParams.setTrackId(trckid);
		return getEnCryptedDetails(payGParams);
	}

	public String getEnCryptedDetails(PayGParams payGParams) {
		return Base64.getEncoder().encodeToString(textEncryptor
				.encrypt(JsonUtil.toJson(payGParams)).getBytes());
	}

	public PayGParams getDeCryptedDetails(String enCryptedDetails) {
		String deCodedDetails = new String(Base64.getDecoder().decode(enCryptedDetails));
		String jsonDetails = textEncryptor.decrypt(deCodedDetails);
		return JsonUtil.fromJson(jsonDetails, PayGParams.class);
	}

	@Deprecated
	public PayGParams getVerifyHash(String trckid, String amount, String docId, String docNo,
			String docFy) {
		PayGParams payGParams = new PayGParams();
		payGParams.setAmount(amount);
		payGParams.setDocId(docId);
		payGParams.setDocNo(docNo);
		payGParams.setDocFy(docFy);
		payGParams.setTrackId(trckid);
		try {
			payGParams.setVerification(CryptoUtil.getMD5Hash((JsonUtil.toJson(payGParams))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return payGParams;
	}

	public PayGParams getVerifyHash(String trckid, String amount, String docId, String docNo,
			String docFy, String payId) {
		PayGParams payGParams = new PayGParams();
		payGParams.setAmount(amount);
		payGParams.setDocId(docId);
		payGParams.setDocNo(docNo);
		payGParams.setDocFy(docFy);
		payGParams.setTrackId(trckid);
		payGParams.setPayId(payId);
		return setVerifyHash(payGParams);
	}

	public PayGParams setVerifyHash(PayGParams payGParams) {
		try {
			payGParams.setSourceToken(generateSourceToken(payGParams));
			payGParams.setVerification(CryptoUtil.getMD5Hash((JsonUtil.toJson(payGParams))));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return payGParams;
	}

	public String generateSourceToken(PayGParams payGParams) {
		return CryptoUtil.getHashBuilder().secret(PayGParams.class.getName())
				.interval(PayGConstants.TOKEN_INTERVAL)
				.message(String.format("%s-%s-%s-%s",
						payGParams.getPayId(), payGParams.getDocId(), payGParams.getTrackId(),
						payGParams.getAmount()))
				.toHMAC().output();
	}

	public ApiResponse<PaymentResponseDto, Object> savePayMentDetails(PayGParams params,
			PaymentResponseDto paymentResponseDto)
			throws Exception {
		try {
			CommonRequestMetaInfo metaInfo = new CommonRequestMetaInfo();
			HttpHeaders headers = new HttpHeaders();

			metaInfo.set("tenant", TenantContextHolder.currentSite());
			metaInfo.set("countryId", paymentResponseDto.getApplicationCountryId());
			metaInfo.set("customerId", paymentResponseDto.getCustomerId());

			headers.add(AppConstants.META_XKEY, new ObjectMapper().writeValueAsString(metaInfo.map()));
			LOGGER.info("amount in params :" + params.getAmount());
			// paymentResponseDto.setAmount(params.getAmount());
			paymentResponseDto.importFrom(params);

			if (ArgUtil.isEmpty(params.getProduct())) {
				return restService.ajax(appConfig.getJaxURL() + "/remit/save-remittance/")
						.post(new HttpEntity<PaymentResponseDto>(paymentResponseDto, headers))
						.as(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto, Object>>() {
						});
			} else {
				LOGGER.info("paymentResponseDto :" + JsonUtil.toJson(paymentResponseDto));
				return restService.ajax(appConfig.getJaxURL()).path(PaymentResponseDto.PAYMENT_CAPTURE_URL)
						.queryParam(PayGParams.PayGConstants.PRODUCT, params.getProduct())
						.queryParam(PayGParams.PayGConstants.CHANNEL, params.getChannel())
						.post(new HttpEntity<PaymentResponseDto>(paymentResponseDto, headers))
						.as(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto, Object>>() {
						});
			}

		} catch (Exception e) {
			LOGGER.error("exception in saveRemittanceTransaction : ", e);
			return AmxApiException.evaluate(e);
		} // end of try-catch

	}
	
	public ApiResponse<PaymentResponseDto, Object> savePayMentFailureDetails(PaymentResponseDto paymentResponseDto)
			throws Exception {
		try {
			CommonRequestMetaInfo metaInfo = new CommonRequestMetaInfo();
			HttpHeaders headers = new HttpHeaders();

			metaInfo.set("tenant", TenantContextHolder.currentSite());
			metaInfo.set("countryId", paymentResponseDto.getApplicationCountryId());
			metaInfo.set("customerId", paymentResponseDto.getCustomerId());

			headers.add(AppConstants.META_XKEY, new ObjectMapper().writeValueAsString(metaInfo.map()));
			
			return restService.ajax(appConfig.getJaxURL()).path(PaymentResponseDto.PAYMENT_IPOS_FAIL_URL)
					.post(new HttpEntity<PaymentResponseDto>(paymentResponseDto, headers))
					.as(new ParameterizedTypeReference<ApiResponse<PaymentResponseDto, Object>>() {
					});
			
		} catch (Exception e) {
			LOGGER.error("exception in saveRemittanceTransaction : ", e);
		}
		
		return null;
	}
}
