package com.boot.jx.postman.converter;

import com.boot.jx.postman.model.File;

import net.sf.jasperreports.engine.JRException;

/**
 * The Interface FileConverter.
 */
public interface FileConverter {

	/**
	 * To PDF.
	 *
	 * @param file
	 *            the file
	 * @return the file
	 * @throws JRException
	 *             the JR exception
	 */
	public File toPDF(File file) throws JRException;
}
