package com.boot.jx.test;

import java.net.URL;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ticket.CustomerTicketDoc;
import com.boot.utils.FileUtil;
import com.boot.utils.JsonUtil;

@SpringBootTest
public class JsonConversionTests {

//	public static void main(String[] arg) {
//		new JsonConversionTests().curl();
//		new JsonConversionTests().chatSessionDTO();
//	}


	@Test
	public void chatSessionDoc() {
		// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");
		URL url = FileUtil.getResource("sample/chat_session_doc.json", JsonConversionTests.class);
		String json = FileUtil.read(url);

		ChatSessionDoc dto = JsonUtil.parse(json, ChatSessionDoc.class);
		System.out.println(JsonUtil.toJson(dto));
	}

	@Test
	public void customTicketDoc() {
		// assertEquals("t1", StringUtils.trim("/abc/def/ghij", '/'), "abc/def/ghij");
		URL url = FileUtil.getResource("sample/chat_session_doc.json", JsonConversionTests.class);
		String json = FileUtil.read(url);

		CustomerTicketDoc dto = JsonUtil.parse(json, CustomerTicketDoc.class);
		System.out.println(JsonUtil.toJson(dto));
	}

}
