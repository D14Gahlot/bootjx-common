package com.boot.jx.postman.converter.jasper;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

/**
 * The Class SimpleReportExporter.
 */
public class SimpleReportExporter {

	/** The jasper print. */
	private JasperPrint jasperPrint;

	/**
	 * Instantiates a new simple report exporter.
	 */
	public SimpleReportExporter() {
	}

	/**
	 * Instantiates a new simple report exporter.
	 *
	 * @param jasperPrint
	 *            the jasper print
	 */
	public SimpleReportExporter(JasperPrint jasperPrint) {
		this.jasperPrint = jasperPrint;
	}

	/**
	 * Gets the jasper print.
	 *
	 * @return the jasper print
	 */
	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}

	/**
	 * Sets the jasper print.
	 *
	 * @param jasperPrint
	 *            the new jasper print
	 */
	public void setJasperPrint(JasperPrint jasperPrint) {
		this.jasperPrint = jasperPrint;
	}

	/**
	 * Export to pdf.
	 *
	 * @param fileName
	 *            the file name
	 * @param author
	 *            the author
	 */
	public void exportToPdf(String fileName, String author) {

		// print report to file
		JRPdfExporter exporter = new JRPdfExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName));

		SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
		reportConfig.setSizePageToContent(true);
		reportConfig.setForceLineBreakPolicy(false);

		SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
		exportConfig.setMetadataAuthor(author);
		exportConfig.setEncrypted(true);
		exportConfig.setAllowedPermissionsHint("PRINTING");

		exporter.setConfiguration(reportConfig);
		exporter.setConfiguration(exportConfig);
		try {
			exporter.exportReport();
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Export to xlsx.
	 *
	 * @param fileName
	 *            the file name
	 * @param sheetName
	 *            the sheet name
	 */
	public void exportToXlsx(String fileName, String sheetName) {
		JRXlsxExporter exporter = new JRXlsxExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName));

		SimpleXlsxReportConfiguration reportConfig = new SimpleXlsxReportConfiguration();
		reportConfig.setSheetNames(new String[] { sheetName });

		exporter.setConfiguration(reportConfig);

		try {
			exporter.exportReport();
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Export to csv.
	 *
	 * @param fileName
	 *            the file name
	 */
	public void exportToCsv(String fileName) {
		JRCsvExporter exporter = new JRCsvExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleWriterExporterOutput(fileName));

		try {
			exporter.exportReport();
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Export to html.
	 *
	 * @param fileName
	 *            the file name
	 */
	public void exportToHtml(String fileName) {
		HtmlExporter exporter = new HtmlExporter();

		exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		exporter.setExporterOutput(new SimpleHtmlExporterOutput(fileName));

		try {
			exporter.exportReport();
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}