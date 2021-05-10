package com.boot.jx.postman.converter;

import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.model.PostManFile;

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
	public CommonFile toPDF(PostManFile file) throws JRException;
}
