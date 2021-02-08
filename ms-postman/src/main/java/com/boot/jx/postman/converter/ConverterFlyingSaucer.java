package com.boot.jx.postman.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.File.FileFormat;
import com.boot.utils.Constants;
import com.codahale.metrics.annotation.Timed;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

import net.sf.jasperreports.engine.JRException;

/**
 * The Class ConverterFlyingSaucer.
 */
@Component
public class ConverterFlyingSaucer implements FileConverter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterFlyingSaucer.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.converter.FileConverter#toPDF(com.amx.jax.postman.model.
	 * File)
	 */
	@Override
	@Timed(name = "PDF_CREATION_FS", absolute = true)
	public File toPDF(File file) throws JRException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {

			if (file.getContent() != null && !Constants.BLANK.equals(file.getContent())) {
				ITextRenderer renderer = new ITextRenderer();
				renderer.getFontResolver().addFont("/fonts/all/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				renderer.getFontResolver().addFont("/fonts/all/arialbd.ttf", BaseFont.IDENTITY_H,
						BaseFont.NOT_EMBEDDED);
				// renderer.getFontResolver().addFont("/fonts/all/arialbi.ttf",
				// BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
				// renderer.getFontResolver().addFont("/fonts/all/ariali.ttf",
				// BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
				renderer.getFontResolver().addFont("/fonts/all/arialuni.ttf", BaseFont.IDENTITY_H,
						BaseFont.NOT_EMBEDDED);
				/*
				 * renderer.getFontResolver().addFont("/fonts/all/times.ttf",
				 * BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				 * renderer.getFontResolver().addFont("/fonts/all/timesbd.ttf",
				 * BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				 * renderer.getFontResolver().addFont("/fonts/all/timesbi.ttf",
				 * BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				 * renderer.getFontResolver().addFont("/fonts/all/timesi.ttf",
				 * BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				 */
				// renderer.getFontResolver().addFontDirectory("/fonts/all",
				// BaseFont.NOT_EMBEDDED);

				renderer.getFontResolver().addFont("/fonts/unhinted/NotoNaskhArabic-Regular2.ttf", BaseFont.IDENTITY_H,
						BaseFont.EMBEDDED);
				renderer.getFontResolver().addFont("/fonts/unhinted/NotoSansGurmukhi-Regular.ttf", BaseFont.IDENTITY_H,
						BaseFont.EMBEDDED);

				// renderer.getFontResolver().addFont("/fonts/all/times.ttf",
				// BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				// renderer.getFontResolver().addFont("/fonts/all/timesbd.ttf",
				// BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				// renderer.getFontResolver().addFont("/fonts/all/timesbi.ttf",
				// BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
				// renderer.getFontResolver().addFont("/fonts/all/timesi.ttf",
				// BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

				renderer.setDocumentFromString(file.getContent());

				renderer.layout();
				renderer.createPDF(outputStream);
				renderer.finishPDF();
				file.setBody(outputStream.toByteArray());
				file.setFileFormat(FileFormat.PNG);
			}
		} catch (DocumentException | IOException e) {
			LOGGER.error("Some Error", e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					/* ignore */ }
			}
		}
		return file;
	}
}
