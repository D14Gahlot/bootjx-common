package com.boot.jx.postman.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.Language;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.client.DocServiceClient;
import com.boot.jx.postman.model.DocResult;
import com.boot.jx.postman.service.CivilIdValidationService;

/**
 * The Class GeoServiceController.
 */
@RestController
public class DocServiceController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocServiceController.class);

	/** The geo location service. */
	@Autowired
	DocServiceClient documentService;

	@Autowired
	CivilIdValidationService civilIdValidationService;

	@RequestMapping(value = PostManUrls.DOC_UPLOAD_URL, method = RequestMethod.POST)
	public String generateUploadUrl(@RequestParam String dir, @RequestParam String type,
			@RequestParam String docid)
			throws PostManException, IOException, URISyntaxException {
		return documentService.generateUrl(dir, type, docid);
	}

	@RequestMapping(value = PostManUrls.DOC_URL_BY_ID, method = RequestMethod.GET)
	public String generateViewUrl(@PathVariable(value = "image_id") String imageId,
			@PathVariable(value = "ext") String ext)
			throws PostManException, IOException, URISyntaxException {
		String x = documentService.imageUrl(imageId, ext);
		LOGGER.info("URL==" + x);
		return x;
	}

	@RequestMapping(value = PostManUrls.DOC_VALIDATE_ID, method = RequestMethod.POST)
	public DocResult validateId(@RequestParam String id)
			throws IOException {
		return civilIdValidationService.validateId(id);
	}

	@SuppressWarnings("static-access")
	@RequestMapping(value = PostManUrls.DOC_SCAN_ID, method = { RequestMethod.POST })
	public Map<String, Object> scanId(@RequestParam MultipartFile file,
			@RequestParam Language lang) throws Exception {
		return civilIdValidationService.scanId(file, lang);
	}

	@RequestMapping(value = PostManUrls.DOC_UPLOAD_FILE, method = { RequestMethod.POST })
	public ApiResponse<DocResult, Object> uploadServiceProviderFile(
			@RequestParam Language lang,
			@RequestParam String dir, @RequestParam String type,
			@RequestParam String docid,
			@RequestParam MultipartFile file,
			@RequestParam(required = false) MultipartFile fileback) throws Exception {
		return ApiResponse
				.buildResults(documentService.scan(AppContextUtil.getTraceId(), dir, type, docid, file, fileback));
	}

	@RequestMapping(value = PostManUrls.DOC_IMAGE_BY_ID, method = { RequestMethod.GET })
	public ApiResponse<DocResult, Object> uploadServiceProviderFile(
			@PathVariable(value = "image_id") String imageId,
			@PathVariable(value = "ext") String ext) throws Exception {
		return ApiResponse
				.buildResults(documentService.imageJson(imageId));
	}
}
