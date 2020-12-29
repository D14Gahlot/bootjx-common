package com.boot.jx.postman.converter.jasper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * The Class SimpleReportFiller.
 */
public class SimpleReportFiller {

	/** The report file name. */
	private String reportFileName;

	/** The jasper report. */
	private JasperReport jasperReport;

	/** The jasper print. */
	private JasperPrint jasperPrint;

	/** The data source. */
	// @Autowired
	private DataSource dataSource;

	/** The parameters. */
	private Map<String, Object> parameters;

	/**
	 * Instantiates a new simple report filler.
	 */
	public SimpleReportFiller() {
		parameters = new HashMap<>();
	}

	/**
	 * Prepare report.
	 */
	public void prepareReport() {
		compileReport();
		fillReport();
	}

	/**
	 * Compile report.
	 */
	public void compileReport() {
		try {
			InputStream reportStream = getClass().getResourceAsStream("/".concat(reportFileName));
			jasperReport = JasperCompileManager.compileReport(reportStream);
			// JRSaver.saveObject(jasperReport, reportFileName.replace(".jrxml",
			// ".jasper"));
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Fill report.
	 */
	public void fillReport() {
		try {
			jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource(1));
		} catch (JRException ex) {
			Logger.getLogger(SimpleReportFiller.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the data source.
	 *
	 * @param dataSource
	 *            the new data source
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Gets the parameters.
	 *
	 * @return the parameters
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * Sets the parameters.
	 *
	 * @param parameters
	 *            the parameters
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the report file name.
	 *
	 * @return the report file name
	 */
	public String getReportFileName() {
		return reportFileName;
	}

	/**
	 * Sets the report file name.
	 *
	 * @param reportFileName
	 *            the new report file name
	 */
	public void setReportFileName(String reportFileName) {
		this.reportFileName = reportFileName;
	}

	/**
	 * Gets the jasper print.
	 *
	 * @return the jasper print
	 */
	public JasperPrint getJasperPrint() {
		return jasperPrint;
	}

}