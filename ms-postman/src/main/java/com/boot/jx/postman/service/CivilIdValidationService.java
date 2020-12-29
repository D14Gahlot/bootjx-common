package com.boot.jx.postman.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.dict.Language;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.model.DocResult;
import com.boot.jx.postman.model.File.Type;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonPath;
import com.boot.utils.StringUtils.StringMatcher;
import com.boot.utils.TimeUtils;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component
public class CivilIdValidationService {

	private static final String UAE_XCHANGE_URL = "https://www.e.gov.kw/sites/kgoEnglish/Pages/eServices/PACI/CivilIDStatus.aspx";

	private static final String PARAM_NAME = "name";
	private static final String TAG_INPUT = "input";

	public static final Pattern VALID_CARD = Pattern.compile("you are holding a valid Civil ID card (.*)$");
	public static final Pattern INVALID_CARD = Pattern.compile("^Validation Failure: Not A Valid Civil Number");
	public static final Pattern INVALID_CARD_RECEIPT = Pattern.compile(
			"^Validation Failure: Card supplied has invalid length - Neither a receipt number nor a valid civil id number");
	public static final Pattern INVALID_RECEIPT = Pattern
			.compile("^Validation Failure: Not a valid Card Receipt Number");

	XmlMapper xmlMapper = new XmlMapper();

	public static final Logger LOGGER = LoggerService.getLogger(CivilIdValidationService.class);

	public DocResult validateId(String identity) throws IOException {

		Document doc0 = Jsoup.connect(UAE_XCHANGE_URL).get();

		Connection con1 = Jsoup.connect(UAE_XCHANGE_URL)
				.referrer(UAE_XCHANGE_URL);

		Elements inputs1 = doc0.select(TAG_INPUT);
		for (Element input : inputs1) {
			String key = input.attr(PARAM_NAME);
			if (!ArgUtil.isEmpty(key)) {
				if (key.endsWith("txtCivilID")) {
					con1 = con1.data(key, identity);
				} else {
					con1 = con1.data(key, input.val());
				}
			}

		}

		Document doc1 = con1.post();

		DocResult resp = new DocResult();
		resp.docId = identity;
		resp.status = "INVALID";

		Elements trs = doc1.select(".section.group .alert .labelText");
		for (Element divs : trs) {
			String key = divs.id();
			if (!ArgUtil.isEmpty(key) && key.endsWith("_lblResult")) {
				resp.response = divs.text();
				LOGGER.debug("{} ==  {}", key, divs.text());
				break;
			}
		}

		StringMatcher matcher = new StringMatcher(resp.response);

		if (matcher.isMatch(VALID_CARD)) {
			resp.status = "VALID_CARD";
		} else if (matcher.isMatch(INVALID_CARD)) {
			resp.status = "INVALID_CARD";
		} else if (matcher.isMatch(INVALID_CARD_RECEIPT) || matcher.isMatch(INVALID_RECEIPT)) {
			resp.status = "INVALID_INPUT";
		} else {
			resp.status = "EXPIRED";
		}

		return resp;
	}

	private static final String PACI_URL = "https://www.paci.gov.kw/Default.aspx";

	@Deprecated
	public void validateCaptcha(String identity) throws IOException {

		Connection con0 = Jsoup.connect(PACI_URL)
				.header("Accept-Encoding", "gzip, deflate")
				.userAgent(
						"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
				// .referrer(PACI_URL)
				.cookie("ASP.NET_SessionId", "001nvimnicv0j32oz5cjejzk")
				.cookie("PaciSite", "en")
				.cookie("TermsAndConditions", "1")
				.maxBodySize(0)
				.timeout(600000);
		Connection.Response response = con0.execute();

		Document doc0 = response.parse();

		// Image Reading Start
		Elements img = doc0.select("#ContentPlaceHolder1_MyCaptchaBot_ImgCaptcha");
		String imageSrc = img.attr("src");
		Connection.Response resultImageResponse = Jsoup.connect("https://www.paci.gov.kw/" + imageSrc)
				.cookies(response.cookies())
				.ignoreContentType(true)
				.method(Connection.Method.GET).timeout(30000).execute();

		File temp = File.createTempFile(identity, ".jpg");
		FileOutputStream out = (new FileOutputStream(temp));
		out.write(resultImageResponse.bodyAsBytes());
		out.close();
		/**
		 * LOGGER.info("Captch form {} is {}", temp.getAbsolutePath(),
		 * CaptchaSolver.INSTANCE.solve(temp.getAbsolutePath()));
		 */
		// Image Reading End

		Connection con1 = con0
				.referrer(PACI_URL);

		Elements inputs1 = doc0.select(TAG_INPUT);
		for (Element input : inputs1) {
			String key = input.attr(PARAM_NAME);
			if (!ArgUtil.isEmpty(key)) {
				if (key.endsWith("CivilNoTxt")) {
					con1 = con1.data(key, identity);
				} else {
					con1 = con1.data(key, input.val());
				}
				// LOGGER.info("INPUT {} {} ", key,input.val() );
			}

		}

		con1.cookie("PaciSite", "en");
		con1.cookie("TermsAndConditions", "1");
		con1.cookies(response.cookies());

		Document doc1 = con1.post();

		Elements trs = doc1.select("#ContentPlaceHolder1_alrtDiv #ContentPlaceHolder1_spanMsg");
		for (Element divs : trs) {
			LOGGER.info("Result+ {}", divs.text());
		}
	}

	@Autowired
	RestService restService;

	private static final JsonPath PARSED_TEXT = new JsonPath("ParsedResults/[0]/ParsedText");
	private static final JsonPath PARSED_ERROR = new JsonPath("ParsedResults/[0]/ParsedText");
	private static final Pattern FIND_CIVIL_ID = Pattern.compile("Civil ID No[\\n ](\\d{12})\n");
	private static final Pattern FIND_MRZ_CODE = Pattern.compile("IDKWT(.+)[\\n](.+)[\\n](.+)");
	private static final Pattern FIND_NAME = Pattern.compile("Name ([a-zA-Z ]+)\n");
	private static final Pattern FIND_NATIONALITY = Pattern.compile("Nationality ([a-zA-Z]{1,3})\n");
	private static final Pattern FIND_DATE = Pattern.compile("\n(\\d{2}/\\d{2}/\\d{4})");
	public static final String FIND_DATE_FORMAT_STRING = "dd/MM/yyyy";
	public static final SimpleDateFormat FIND_DATE_FORMAT = new SimpleDateFormat(FIND_DATE_FORMAT_STRING);

	public Map<String, Object> scanId(MultipartFile file, Language lang) throws IOException {
		Type type = com.boot.jx.postman.model.File.Type.from(file.getContentType());
		String fileType = "." + type.toString().toLowerCase();
		System.out.println("fileType==" + fileType + "  -  " + file.getName());
		long startTime = System.currentTimeMillis();
		int ocrEngine = 2;
		switch (type) {
		case TIFF:
			ocrEngine = 1;
			break;
		default:
			break;
		}

		Map<String, Object> resp = restService.ajax("https://api.ocr.space/parse/image")
				.field("apikey", "b21643ec2c88957")
				.field("language", lang.getISO3Code())
				.field("OCREngine", ocrEngine)
				.field("FileType", fileType)
				.field("fileType", fileType)
				.field("scale", true)
				.field("detectOrientation", true)
				.field("file", file)
				.queryParam("fileType", fileType)
				// .resource("file", restService.getByteArrayFile(file))
				.postForm()
				.as(new ParameterizedTypeReference<Map<String, Object>>() {
				});

		Map<String, Object> output = new HashMap<String, Object>();
		String parsedText = PARSED_TEXT.load(resp, "");

		output.put("text", parsedText);
		System.out.println(parsedText);

		StringMatcher matcher = new StringMatcher(parsedText);
		if (matcher.isMatch(FIND_CIVIL_ID)) {
			output.put("identity", matcher.group(1));
		}

		if (matcher.isMatch(FIND_NAME)) {
			output.put("name", matcher.group(1));
		}

		if (matcher.isMatch(FIND_DATE)) {

			Date now = new Date();
			Date dob = getDate(matcher.group(1));
			Date expiry = null;

			if (dob.after(now)) {
				expiry = dob;
				dob = null;
			}

			if (matcher.find()) {
				Date date2 = getDate(matcher.group(1));
				if (expiry == null) {
					expiry = date2;
				} else if (date2 != null && date2.after(expiry)) {
					expiry = date2;
				} else if (dob == null && date2.before(now)) {
					dob = date2;
				}
			}

			if (ArgUtil.is(dob)) {
				output.put("dob", FIND_DATE_FORMAT.format(dob));
			}

			if (ArgUtil.is(expiry)) {
				output.put("expiry", FIND_DATE_FORMAT.format(expiry));
			}

			output.put("timeTaken", TimeUtils.timeSince(startTime));
		}

		if (matcher.isMatch(FIND_MRZ_CODE)) {
			// MrzRecord record = MrzParser.parse(matcher.group(0));
		}

		return output;
	}

	public static Date getDate(String date) {
		try {
			return FIND_DATE_FORMAT.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
