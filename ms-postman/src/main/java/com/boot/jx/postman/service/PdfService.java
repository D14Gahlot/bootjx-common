package com.boot.jx.postman.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.converter.FileConverter;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.File.PDFConverter;

import net.sf.jasperreports.engine.JRException;

/**
 * The Class PdfService.
 */
@Component
public class PdfService {

	/** The converter flying saucer. */
	@Autowired
	private FileConverter converterFlyingSaucer;

	/** The converter amx flying saucer. */
	@Autowired
	private FileConverter converterAmxFlyingSaucer;

	/** The converter jasper. */
	@Autowired
	private FileConverter converterJasper;

	// @Autowired
	// private ConverterIText7 converterIText7;

	/** The p DF converter. */
	@Value("${default.pDFConverter}")
	private PDFConverter pDFConverter;

	// @Autowired
	// private ConverterIText5 converterIText5;

	/**
	 * Convert.
	 *
	 * @param file
	 *            the file
	 * @return the file
	 * @throws JRException
	 *             the JR exception
	 */
	public File convert(File file) throws JRException {

		PDFConverter conv = file.getConverter();
		if (conv == null) {
			conv = file.getITemplate().getConverter();
			if (conv == null) {
				conv = pDFConverter;
			}
		}

		if (conv == PDFConverter.FS) {
			return converterFlyingSaucer.toPDF(file);
		} else if (conv == PDFConverter.AMXFS) {
			return converterAmxFlyingSaucer.toPDF(file);
			// } else if (conv == PDFConverter.ITEXT5) {
			// return converterIText5.toPDF(file);
		} else if (conv == PDFConverter.JASPER) {
			return converterJasper.toPDF(file);
			// } else if (conv == PDFConverter.ITEXT7) {
			// return converterIText7.toPDF(file);
		} else {
			return converterFlyingSaucer.toPDF(file);
		}
		// return file;
	}

}
