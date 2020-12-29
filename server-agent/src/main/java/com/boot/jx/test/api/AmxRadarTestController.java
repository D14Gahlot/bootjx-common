package com.boot.jx.test.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.rest.RestService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Controller
public class AmxRadarTestController {

	@Autowired
	private RestService restService;

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@ResponseBody
	@RequestMapping(value = "/pub/test", method = { RequestMethod.POST, RequestMethod.GET })
	public SampleSenderReply postVote(@RequestParam String xyz) {

		System.out.println(xyz);
		System.out.println(commonHttpRequest.get("xyz"));

		return restService.ajax(
				"http://localhost/dist/sample.xml").get().as(SampleSenderReply.class);
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SampleSenderReply {
		Map<String, String> channel;
		public SampleSender sender;

		public Map<String, String> getChannel() {
			return channel;
		}

		public void setChannel(Map<String, String> channel) {
			this.channel = channel;
		}
	}

	public static class SampleSender {
		public SampleName name;
	}

	public static class SampleName {
		public String nameType;
		public String firstName;
		public String lastName;
	}

}
