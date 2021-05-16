package com.boot.jx.postman.converter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.boot.jx.dict.FileFormat;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.model.PostManFile;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

import gui.ava.html.Html2Image;
import net.sf.jasperreports.engine.JRException;

/**
 * The Class ConverterAmxFlyingSaucer.
 */
@Component
public class ConverterAmxFlyingSaucer implements FileConverter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConverterAmxFlyingSaucer.class);

	/**
	 * Instantiates a new converter amx flying saucer.
	 */
	public ConverterAmxFlyingSaucer() {
		// com.itextpdf.licensekey.LicenseKey.loadLicenseFile(getClass().getResourceAsStream("/license/itextkey1520345049511_0.xml"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.converter.FileConverter#toPDF(com.amx.jax.postman.model.
	 * File)
	 */
	@Override
	public CommonFile toPDF(PostManFile file) throws JRException {
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

				Document doc = renderer.getDocument();

				NodeList list = renderer.getDocument().getElementsByTagName("html2img");
				Transformer transformer = TransformerFactory.newInstance().newTransformer();

				for (int i = 0; i < list.getLength(); i++) {
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					StringWriter writer = new StringWriter();
					Node node = list.item(i);

					transformer.transform(new DOMSource(node), new StreamResult(writer));
					String html = writer.toString();
					// html = html.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
					LOGGER.info("====HTML=={}", html);

					Html2Image img = Html2Image.fromHtml(html);

					// img.getImageRenderer().setWidth(700);
					String width = ((Element) node).getAttribute("width");
					if (width != null && !width.isEmpty()) {
						img.getImageRenderer().setWidth(ArgUtil.parseAsInteger(width));
					}
					String height = ((Element) node).getAttribute("height");
					if (height != null && !height.isEmpty()) {
						img.getImageRenderer().setHeight(ArgUtil.parseAsInteger(height));
					}
					// LOGGER.info("====WIDTH=={},{}x{}",width,img.getImageRenderer().getWidth(),img.getImageRenderer().getHeight());
					// img.getImageRenderer().getRootBox().setStyle(style);
					// BufferedImage imageFromConvert = img.getImageRenderer().getBufferedImage();
					// ImageIO.write(imageFromConvert, "png", os);
					img.getImageRenderer().saveImage(os, true);
					Element imgtag = doc.createElement("img");
					// LOGGER.info("====IMG=={}","data:image/png;;base64,"
					// + StringUtils.newStringUtf8(Base64.encodeBase64(os.toByteArray(), false)));
					imgtag.setAttribute("src", "data:image/png;;base64,"
							+ StringUtils.newStringUtf8(Base64.encodeBase64(os.toByteArray(), false)));
					node.removeChild(node.getFirstChild());
					// doc.replaceChild(imgtag, node);
					// doc.appendChild(node);
					node.replaceChild(imgtag, node.getFirstChild());
				}
				renderer.layout();
				renderer.createPDF(outputStream);
				renderer.finishPDF();
				file.setBody(outputStream.toByteArray());
				file.setFileFormat(FileFormat.PNG);
			}
		} catch (DocumentException | IOException | TransformerException e) {
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
