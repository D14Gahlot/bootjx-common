package com.boot.jx.media;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JEditorPane;

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

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.cdn.BootJxConfigService;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.Encoder;

import gui.ava.html.image.generator.HtmlImageGenerator;
import io.swagger.annotations.ApiParam;

@Controller
public class MediaController {

	private static final Logger LOGGER = LoggerFactory.getLogger(MediaController.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Autowired(required = false)
	private BootJxConfigService bootJxConfigService;

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image", method = { RequestMethod.POST })
	public ResponseEntity<byte[]> html2ImagePost(

			@ApiParam @RequestParam String text) throws FileNotFoundException, IOException {

		File file = File.createTempFile("tmp", ".png");
//		Converter.convertHTML("<h1>Convert HTML to Image in Java</h1>", ".", new ImageSaveOptions(ImageFormat.Jpeg),
//				file.getAbsolutePath());

		HtmlImageGenerator imageGenerator = new HtmlImageGenerator() {
			protected JEditorPane createJEditorPane() {
				JEditorPane editor = super.createJEditorPane();
				editor.setOpaque(false); // The solution
				return editor;
			}
		};
		imageGenerator.loadHtml("<div style='width:400px;min-height:400px;'>" + text + "</div>");
		imageGenerator.saveAsImage(file);

		byte[] image = Files.readAllBytes(file.toPath());
		return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(image);
	}

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image", method = { RequestMethod.GET })
	public ResponseEntity<byte[]> html2ImageGet(@ApiParam @RequestParam String encodedHtml)
			throws FileNotFoundException, IOException {
		String html = CryptoUtil.getEncoder().message(encodedHtml).decodeBase64().toString();
		return this.html2ImagePost(html);
	}

	@ResponseBody
	@RequestMapping(value = "/media/text/to/image/{encodedHtml}", method = { RequestMethod.GET })
	public ResponseEntity<byte[]> html2ImageURL(@ApiParam @PathVariable String encodedHtml)
			throws FileNotFoundException, IOException {
		return this.html2ImageGet(encodedHtml);
	}

}
