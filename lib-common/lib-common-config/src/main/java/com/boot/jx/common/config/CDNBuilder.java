package com.boot.jx.common.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.LoggerService;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;

@Component
public class CDNBuilder {

    private static final Logger LOGGER = LoggerService.getLogger(CDNBuilder.class);

    private static final String LATEST = "LATEST";
    public static Pattern PATTERN = Pattern
	    .compile("(?<proto>.+)cdn.jsdelivr.net/gh/(?<org>.+)/(?<repo>.+)@(?<version>[-a-zA-Z0-9\\.]+)(?<path>.*)");
    public static String VERSION_URL = "https://api.github.com/repos/%s/%s/commits/%s?page=0&per_page=1";

    private Map<String, String> cdnMapper = new ConcurrentHashMap<String, String>();

    public String latest(String cdnUrl) {
	String cdnUrlNew = cdnMapper.get(cdnUrl);
	if (LATEST.equals(cdnUrlNew)) {
	    return cdnUrl;
	} else if (ArgUtil.is(cdnUrlNew)) {
	    return cdnUrlNew;
	} else {
	    cdnMapper.put(cdnUrl, cdnUrl);
	}
	return cdnUrl;
    }

    public String updateVersion(String oldUrl, String version) {
	Matcher matcher = PATTERN.matcher(oldUrl);
	if (matcher.find()) {
	    String protoV = matcher.group("proto");
	    String orgV = matcher.group("org");
	    String repoV = matcher.group("repo");
	    String versionV = matcher.group("version");
	    String pathV = matcher.group("path");
	    return String.format("%scdn.jsdelivr.net/gh/%s/%s@%s%s", protoV, orgV, repoV, StringUtils.trim(version),
		    pathV);
	}
	return oldUrl;
    }

    @Autowired
    private RestService restService;

    public boolean isValidSHA1(String s) {
	return s.matches("^[a-fA-F0-9]{40}$");
    }

    @Async
    public void update() {
	for (Entry<String, String> cdn : cdnMapper.entrySet()) {
	    if (ArgUtil.areEqual(cdn.getKey(), cdn.getValue())) {
		Matcher matcher = PATTERN.matcher(cdn.getKey());
		if (matcher.find()) {
		    String protoV = matcher.group("proto");
		    String orgV = matcher.group("org");
		    String repoV = matcher.group("repo");
		    String versionV = matcher.group("version");
		    String pathV = matcher.group("path");
		    if (!isValidSHA1(versionV)) {
			String versionUrl = String.format(VERSION_URL, orgV, repoV, versionV);
			String sha = null;
			try {
			    Map<String, Object> resp = restService.ajax(versionUrl).get().asMap();
			    sha = (String) resp.get("sha");
			} catch (Exception e) {
			    LOGGER.error("Errror while fetching CDN version for {}" + versionUrl);
			}
			if (ArgUtil.is(sha) && !versionV.equals(sha)) {
			    String url = String.format("%scdn.jsdelivr.net/gh/%s/%s@%s%s", protoV, orgV, repoV,
				    StringUtils.trim(sha), pathV);
			    cdnMapper.put(cdn.getKey(), url);
			} else {
			    cdnMapper.put(cdn.getKey(), LATEST);
			}
		    }

		}
	    }

	}
    }

    @Scheduled(fixedDelay = 5000)
    public void updateJob() {
	this.update();
    }

}
