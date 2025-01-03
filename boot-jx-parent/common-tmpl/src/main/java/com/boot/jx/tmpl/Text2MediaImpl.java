package com.boot.jx.tmpl;

import static org.mockito.ArgumentMatchers.byteThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.model.CommonFileStream;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PostmanPackages.Text2Media;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

import gui.ava.html.image.generator.HtmlImageGenerator;

@Component
public class Text2MediaImpl implements Text2Media {

	Pattern FIND_ENGIN = Pattern.compile("--engine:([^;]+);");

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Value("${mry.chrono.url}")
	private String cronoJobUrl;

	@Autowired
	private RestService restService;

	private CommonFileStream chrono(String text, String style) throws IOException {
		File file = File.createTempFile("tmp", ".png");
		byte[] image = restService.ajax(cronoJobUrl).path("/worker/api/v1/html-to-image")//
				// .path("/worker/api/v1/html-to-image")//
				.accept("image/*")//
				.postJson(MapModel.createInstance().put("htmlContent", text)//
						// .put("text", text)//
						.put("height", "400")//
						.put("width", "400")//
						.put("jsonData", MapModel.createInstance().toMap())//
						.put("style", style).toMap())
				.asByteArray();
//		if (image == null) {
//			System.out.println("byteNULLLLLLL");
//		}
		CommonFileStream srcFile = new CommonFileStream().body(image).fileType(FileType.IMAGE).format(FileFormat.PNG)
				.name(file.getName());
		return srcFile;
	}

	private CommonFileStream local(String text, String style) throws IOException {
		File file = File.createTempFile("tmp", ".png");
		HtmlImageGenerator imageGenerator = new HtmlImageGenerator() {
			protected JEditorPane createJEditorPane() {
				JEditorPane editor = super.createJEditorPane();
				editor.setOpaque(false); // The solution
				return editor;
			}
		};
		imageGenerator.loadHtml(
				"<div style='" + ArgUtil.nonEmpty(style, DEFAULT_VALUES.MEDIA_TEMPLATE_STYLE) + "'>" + text + "</div>");
		imageGenerator.saveAsImage(file);
		byte[] image = Files.readAllBytes(file.toPath());
		CommonFileStream srcFile = new CommonFileStream().body(image)
				// .fileType(attachment.getMediaType())
				.format(FileFormat.PNG).name(file.getName());
		return srcFile;
	}

	@Override
	public CommonFileStream toImageFile(String text, String style) throws IOException {

		boolean useChrono = false;

		if (ArgUtil.is(style)) {
			Matcher matcher = FIND_ENGIN.matcher(style);
			if (matcher.find()) {
				useChrono = "chrono".equalsIgnoreCase(matcher.group(1));
			}
		}
		if (useChrono) {
			return chrono(text, style);
		}
		return local(text, style);
	}

	@Override
	public String toImage(String text, String style, String textId) throws IOException {

		String md5 = CryptoUtil.getEncoder().message(text).md5().toString();

		CommonFileStream srcFile = toImageFile(text, style);

		// System.out.println("--" + AppContextUtil.getTraceId());
		// System.out.println("--" + AppContextUtil.getSessionIdFromTraceId());

		CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile, textId, md5);
		return dstFile.getUrl();
	}

	@Override
	public String toImage(String text) throws IOException {
		return toImage(text, DEFAULT_VALUES.MEDIA_TEMPLATE_STYLE, AppContextUtil.getSessionIdFromTraceId());
	}

}
