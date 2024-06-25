package com.boot.jx.tmpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JEditorPane;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.model.CommonFile;
import com.boot.jx.model.CommonFileStream;
import com.boot.jx.postman.PMConstants.DEFAULT_VALUES;
import com.boot.jx.postman.PostmanPackages.Text2Media;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.utils.CryptoUtil;

import gui.ava.html.image.generator.HtmlImageGenerator;

@Component
public class Text2MediaImpl implements Text2Media {

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Override
	public String toImage(String text, String textId) throws IOException {

		String md5 = CryptoUtil.getEncoder().message(text).md5().toString();

		File file = File.createTempFile("tmp", ".png");
		HtmlImageGenerator imageGenerator = new HtmlImageGenerator() {
			protected JEditorPane createJEditorPane() {
				JEditorPane editor = super.createJEditorPane();
				editor.setOpaque(false); // The solution
				return editor;
			}
		};

		imageGenerator.loadHtml("<div style='" + DEFAULT_VALUES.MEDIA_TEMPLATE_STYLE + "'>" + text + "</div>");
		imageGenerator.saveAsImage(file);

		byte[] image = Files.readAllBytes(file.toPath());

		CommonFileStream srcFile = new CommonFileStream().body(image)
				// .fileType(attachment.getMediaType())
				.format(FileFormat.PNG).name(file.getName());

		// System.out.println("--" + AppContextUtil.getTraceId());
		// System.out.println("--" + AppContextUtil.getSessionIdFromTraceId());

		CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile, textId, md5);
		return dstFile.getUrl();
	}

	@Override
	public String toImage(String text) throws IOException {
		return toImage(text, AppContextUtil.getSessionIdFromTraceId());
	}

}
