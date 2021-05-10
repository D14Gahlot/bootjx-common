package com.boot.jx.postman.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.postman.dms.DMService;
import com.boot.utils.MapBuilder;
import com.cloudinary.utils.ObjectUtils;

/**
 * The Class GeoServiceController.
 */
@Controller
@RequestMapping("/dms")
public class DMSController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DMSController.class);

	@Autowired
	private DMService dmService;
	

	@RequestMapping(value = "/ext/upload/frame", method = RequestMethod.GET)
	public String uploadFrame(Model model,@RequestParam String pmsign) {
		long timestamp = System.currentTimeMillis();
		Map params = ObjectUtils.asMap(
				"public_id","test1",
				"timestamp" , timestamp/1000
		);
		String signature = dmService.cloudinary().apiSignRequest(params, dmService.getCloudinaryApiSecret());
		
		Map options = ObjectUtils.asMap(
				"cloud_name", dmService.getCloudinaryName(),
				"api_key" ,dmService.getCloudinaryApiKey(),
				"resource_type", "auto",
				"callback", "https://xyz.com/",
				"signature", signature
		);
		String url = dmService.cloudinary().uploader().getUploadUrl(options);
		model.addAttribute("URL", url);
		model.addAttribute("PARAMS", params);
		model.addAttribute("OPTIONS", options);

		return "cloudinary_upload";
	}
	
	@ResponseBody
	@RequestMapping(value = "/ext/upload/options", method = RequestMethod.GET)
	public Map<String, Object> uploadOptions(@RequestParam String publicId) {
		long timestamp = System.currentTimeMillis();
		Map params = ObjectUtils.asMap(
				"public_id",publicId,
				"timestamp" , timestamp/1000
		);
		String signature = dmService.cloudinary().apiSignRequest(params, dmService.getCloudinaryApiSecret());
		
		Map options = ObjectUtils.asMap(
				"cloud_name", dmService.getCloudinaryName(),
				"api_key" ,dmService.getCloudinaryApiKey(),
				"resource_type", "auto",
				"callback", "https://xyz.com/",
				"signature", signature
		);
		String url = dmService.cloudinary().uploader().getUploadUrl(options);
		return MapBuilder.map().put("URL", url).put("PARAMS", params).put("OPTIONS", options).build();
	}

}
