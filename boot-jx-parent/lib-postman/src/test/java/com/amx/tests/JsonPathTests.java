package com.amx.tests;

import java.net.URL;

import org.junit.Test;

import com.boot.jx.postman.wa360.WA360Constants.InBoundWrapperPaths;
import com.boot.model.MapModel;
import com.boot.utils.FileUtil;
import com.boot.utils.JsonUtil;

public class JsonPathTests {

    @Test
    public void wa360d() {
	// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");

	URL url = FileUtil.getResource("sample/wa360d_inbound.json");
	String json = FileUtil.read(url);
	MapModel map = MapModel.from(JsonUtil.fromJsonToMap(json));
	String number = map.entry(InBoundWrapperPaths.CONTACT_NUMBER).asString();

    }

}
