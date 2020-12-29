package com.boot.jx.dict;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.boot.jx.api.ResponseCodeDetailDTO;
import com.boot.jx.scope.TenantContextHolder;

public class PayGRespCodeJSONConverter {

	private static final Logger LOGGER = Logger.getLogger(PayGRespCodeJSONConverter.class);

	public static ResponseCodeDetailDTO getResponseCodeDetail(String errorCategory) {
		ResponseCodeDetailDTO responseCodeDetail = new ResponseCodeDetailDTO();
		if (errorCategory != null) {
			Map<String, String> map = new HashMap<>();
			switch (TenantContextHolder.currentSite()) {
			case BHR:
				ResponseCodeBHR enumBHR = ResponseCodeBHR.valueOf(errorCategory);
				if (enumBHR != null) {
					responseCodeDetail.setResponseCode(enumBHR.getResponseCode());
					responseCodeDetail.setResponseDesc(enumBHR.getResponseDesc());
					responseCodeDetail.setAlmullaErrorCode(enumBHR.getAlmullaErrorCode());
					responseCodeDetail.setCategory(enumBHR.getCategory());
					responseCodeDetail.setType(enumBHR.getCategory().getType().toString());
					responseCodeDetail.setStatus(enumBHR.getCategory().getStatus().toString());

					map.put(enumBHR.getResponseCode(), enumBHR.getResponseDesc());
					responseCodeDetail.setClientResponse(map);

					LOGGER.debug("Response Code Details BHR JSON : " + responseCodeDetail.toString());
				}

				break;
			case OMN:
				ResponseCodeOMN enumOMN = ResponseCodeOMN.valueOf(errorCategory);
				if (enumOMN != null) {
					responseCodeDetail.setResponseCode(enumOMN.getResponseCode());
					responseCodeDetail.setResponseDesc(enumOMN.getResponseDesc());
					responseCodeDetail.setAlmullaErrorCode(enumOMN.getAlmullaErrorCode());
					responseCodeDetail.setCategory(enumOMN.getCategory());
					responseCodeDetail.setType(enumOMN.getCategory().getType().toString());
					responseCodeDetail.setStatus(enumOMN.getCategory().getStatus().toString());

					map.put(enumOMN.getResponseCode(), enumOMN.getResponseDesc());
					responseCodeDetail.setClientResponse(map);

					LOGGER.debug("Response Code Details OMN JSON : " + responseCodeDetail.toString());
				}

				break;
			case KWT:
				ResponseCodeKWT enumKWT = ResponseCodeKWT.valueOf(errorCategory);
				if (enumKWT != null) {
					responseCodeDetail.setResponseCode(enumKWT.getResponseCode());
					responseCodeDetail.setResponseDesc(enumKWT.getResponseDesc());
					responseCodeDetail.setAlmullaErrorCode(enumKWT.getAlmullaErrorCode());
					responseCodeDetail.setCategory(enumKWT.getCategory());
					responseCodeDetail.setType(enumKWT.getCategory().getType().toString());
					responseCodeDetail.setStatus(enumKWT.getCategory().getStatus().toString());

					map.put(enumKWT.getResponseCode(), enumKWT.getResponseDesc());
					responseCodeDetail.setClientResponse(map);

					LOGGER.debug("Response Code Details KWT JSON : " + responseCodeDetail.toString());
				}

				break;	

			default:
				break;
			}
		}
		return responseCodeDetail;

	}
}
