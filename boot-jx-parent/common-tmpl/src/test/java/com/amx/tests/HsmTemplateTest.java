package com.amx.tests;

import java.net.URL;

import org.junit.Test;

import com.boot.jx.tmpl.CommonTmpPackageImpl;
import com.boot.model.MapModel;
import com.boot.utils.FileUtil;
import com.boot.utils.JsonUtil;

public class HsmTemplateTest {

	public static CommonTmpPackageImpl ppkg = new CommonTmpPackageImpl();

	public static void main(String[] arg) {
		new HsmTemplateTest().hsmOptionsCompile();
	}

	@Test
	public void wa360Templates() {
		// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");
		MapModel map = sampleModel();

		System.out.println(ppkg.process("   "
				// + "{{contact.name}} - "
				// + "{{data.customer}} "
				+ "{{any data.customer contact.name}} ", map.toMap(), CommonTmpPackageImpl.HANDLEBARS_JS));
	}

	private MapModel sampleModel() {
		URL url = FileUtil.getResource("sample/sample_model.json");
		String json = FileUtil.read(url);
		MapModel map = MapModel.from(JsonUtil.fromJsonToMap(json));
		return map;
	}

	private MapModel hsmOptions() {
		URL url = FileUtil.getResource("sample/hsm_options.json");
		String json = FileUtil.read(url);
		MapModel map = MapModel.from(JsonUtil.fromJsonToMap(json));
		return map;
	}

	@Test
	public void hsmOptionsCompile() {
		// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");
		String map = JsonUtil.toJson(hsmOptions());
		System.out.println(ppkg.process(map, sampleModel().toMap(), CommonTmpPackageImpl.HANDLEBARS_JS));
	}

}
