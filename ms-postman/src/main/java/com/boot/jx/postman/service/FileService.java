package com.boot.jx.postman.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.util.ByteArrayDataSource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.exceptions.TemplateInputException;

import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditService;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.File.Type;
import com.boot.jx.tmpl.TemplateService;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.utils.ArgUtil;

import net.sf.jasperreports.engine.JRException;

/**
 * The Class FileService.
 */
@Component
public class FileService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerService.getLogger(FileService.class);

	/** The pdf service. */
	@Autowired
	private PdfService pdfService;

	/** The template service. */
	@Autowired
	private TemplateService templateService;

	/** The template utils. */
	@Autowired
	TemplateUtils templateUtils;

	/** The slack service. */
	@Autowired
	SlackService slackService;

	/** The audit service. */
	@Autowired
	AuditService auditService;

	/**
	 * Creates the.
	 *
	 * @param file the file
	 * @return the file
	 */

	public File create(File file) {
		return create(file, null);
	}

	public File create(File file, ContactType contactType) {
		if (file.getTemplate() != null) {
			/**
			 * from template to content
			 */
			try {
				templateService.process(file, contactType);
			} catch (TemplateInputException e) {
				LOGGER.error("Template Process Exception", e);
			}

			if (ArgUtil.isEmptyString(file.getTitle())) {
				file.setTitle(templateUtils.prop("template." + file.getTemplate() + ".title"));
			}

			if (file.getName() == null) {
				if (file.getType() == Type.PDF) {
					file.setName(file.getITemplate().getFileName() + ".pdf");
				} else if (file.getType() == Type.JSON) {
					file.setName(file.getITemplate().getFileName() + ".json");
				} else {
					file.setName(file.getITemplate().getFileName() + ".html");
				}
			}
		}

		if (file.getType() == File.Type.PDF) {
			/**
			 * From string to File type
			 */
			PMGaugeEvent pmGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.CREATE_PDF, file);
			try {
				pdfService.convert(file);
				auditService.gauge(pmGaugeEvent);
			} catch (JRException e) {
				auditService.excep(pmGaugeEvent, LOGGER, e);
			}

		}
		return file;
	}

	/**
	 * To input stream.
	 *
	 * @param file the file
	 * @return the input stream
	 */
	public InputStream toInputStream(File file) {
		// convert bytes into InputStream
		InputStream is = new ByteArrayInputStream(file.getBody());
		// read it with BufferedReader
		// BufferedReader br = new BufferedReader(new InputStreamReader(is));
		//
		// String line;
		// while ((line = br.readLine()) != null) {
		// System.out.println(line);
		// }
		// br.close();

		return is;
	}

	/**
	 * To data source.
	 *
	 * @param file the file
	 * @return the data source
	 * @throws MessagingException the messaging exception
	 */
	public DataSource toDataSource(File file) throws MessagingException {
		DataSource dataSource = new ByteArrayDataSource(file.getBody(), "application/pdf");
		MimeBodyPart pdfBodyPart = new MimeBodyPart();
		pdfBodyPart.setDataHandler(new DataHandler(dataSource));
		pdfBodyPart.setFileName(file.getName());
		return dataSource;
	}
}
