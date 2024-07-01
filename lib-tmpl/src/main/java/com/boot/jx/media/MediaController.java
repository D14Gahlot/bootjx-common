package com.boot.jx.media;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.model.CommonFileStream;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PostmanPackages.Text2Media;
import com.boot.utils.CryptoUtil;

import io.swagger.annotations.ApiParam;

@Controller
public class MediaController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaController.class);

	@Autowired
	protected Text2Media text2Media;

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image", method = { RequestMethod.POST })
	public ResponseEntity<byte[]> html2ImagePost(@ApiParam @RequestParam String text,
			@RequestParam(defaultValue = DEFAULT_VALUES.MEDIA_TEMPLATE_STYLE) String style)
			throws FileNotFoundException, IOException {
		CommonFileStream srcFile = text2Media.toImageFile(text, style);
		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(srcFile.getBody());
	}

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image", method = { RequestMethod.GET })
	public ResponseEntity<byte[]> html2ImageGet(@ApiParam @RequestParam String encodedHtml, @RequestParam String style)
			throws FileNotFoundException, IOException {
		String html = CryptoUtil.getEncoder().message(encodedHtml).decodeBase64().toString();
		return this.html2ImagePost(html, style);
	}

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image/{encodedHtml}", method = { RequestMethod.GET })
	public ResponseEntity<byte[]> html2ImageURL(@ApiParam @PathVariable String encodedHtml, @RequestParam String style)
			throws FileNotFoundException, IOException {
		return this.html2ImageGet(encodedHtml, style);
	}

}
