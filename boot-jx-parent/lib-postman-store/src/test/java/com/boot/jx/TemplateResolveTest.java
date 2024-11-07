package com.boot.jx;

import java.net.URL;
import java.util.List;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.FileUtil;
import com.boot.utils.JsonUtil;

@SpringBootTest
public class TemplateResolveTest {

	public static void main(String[] arg) {
		// new JsonConversionTests().curl();
		new TemplateResolveTest().findTempplate();
	}

	@Test
	public void findTempplate() {

		// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");
		URL url = FileUtil.getResource("sample/hsm_templates.json", TemplateResolveTest.class);
		String json = FileUtil.read(url);

		MapModel x = MapModel.fromSafe(json);
		List<HSMTemplateDoc> temps = x.listOf(HSMTemplateDoc.class);

		System.out.println(JsonUtil.toJson(temps));

		CommonTemplateMeta template = new CommonTemplateMeta();
		// template.setLang("en");
		ContactType contactType = ContactType.WHATSAPP;

		HSMTemplateDoc wildCardTemp = null;
		HSMTemplateDoc exactTemp = null;
		HSMTemplateDoc noLangTemp = null;
		HSMTemplateDoc noContactTemp = null;
		HSMTemplateDoc engLangTemp = null;

		for (HSMTemplateDoc hsmTemplate3rdParty : temps) {
			if (ArgUtil.not(hsmTemplate3rdParty.getContactType()) && ArgUtil.not(hsmTemplate3rdParty.getLang())) {
				wildCardTemp = hsmTemplate3rdParty;
			} else if (ArgUtil.is(hsmTemplate3rdParty.getContactType(), contactType)
					&& ArgUtil.is(hsmTemplate3rdParty.getLang(), template.getLang())) {
				exactTemp = hsmTemplate3rdParty;
				break;
			} else if (ArgUtil.is(hsmTemplate3rdParty.getContactType(), contactType)
					&& (ArgUtil.not(hsmTemplate3rdParty.getLang()) || (ArgUtil.not(noLangTemp)
							&& ArgUtil.is(hsmTemplate3rdParty.getLang(), "en", "en_US", "en_GB")))) {
				noLangTemp = hsmTemplate3rdParty;
			} else if (ArgUtil.not(hsmTemplate3rdParty.getContactType())
					&& ArgUtil.is(hsmTemplate3rdParty.getLang(), template.getLang())) {
				noContactTemp = hsmTemplate3rdParty;
			} else if (ArgUtil.not(hsmTemplate3rdParty.getContactType())
					&& ArgUtil.is(hsmTemplate3rdParty.getLang(), "en", "en_US", "en_GB")) {
				engLangTemp = hsmTemplate3rdParty;
			}
		}

		HSMTemplateDoc resolvedTemplate = ArgUtil.anyOf(exactTemp, noLangTemp, engLangTemp, wildCardTemp,
				noContactTemp);

//		System.out.println("wildCardTemp:  " + JsonUtil.toJson(wildCardTemp));
//		System.out.println("exactTemp:  " + JsonUtil.toJson(exactTemp));
//		System.out.println("noLangTemp:  " + JsonUtil.toJson(noLangTemp));
//		System.out.println("engLangTemp:  " + JsonUtil.toJson(engLangTemp));
		System.out.println("resolvedTemplate:  " + JsonUtil.toJson(resolvedTemplate));
	}

}
