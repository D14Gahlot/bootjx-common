package com.boot.jx.common.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.LoggerService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
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
	    return cdnUrl.replaceAll("\\s", "");
	} else if (ArgUtil.is(cdnUrlNew)) {
	    return cdnUrlNew.replaceAll("\\s", "");
	} else {
	    String fixedCDNUrl = ArgUtil.parseAsString(cdnUrl, Constants.BLANK).replaceAll("\\s", "");
	    cdnMapper.put(cdnUrl, fixedCDNUrl);
	    return cdnUrl;
	}
    }

    public String updateVersion(String oldUrl, String version) {
	version = StringUtils.trim(version);
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

}
