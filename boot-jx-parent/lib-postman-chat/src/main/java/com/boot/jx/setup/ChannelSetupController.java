package com.boot.jx.setup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.cdn.BootJxConfigService;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.doc.config.ChannelConfigSetupDoc;
import com.boot.jx.postman.doc.config.ChannelConfigTempDoc;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;

@Controller
public class ChannelSetupController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChannelSetupController.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired(required = false)
	private BootJxConfigService bootJxConfigService;

	@RequestMapping(value = "/ext/setup/channel", method = { RequestMethod.GET })
	public String setupChannel(@RequestParam(required = false) CHANNEL_TYPE_ENUM channelType,
			@RequestParam(required = false) String channelConfigId,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortDir, Model model)
			throws FileNotFoundException, IOException {

		List<ChannelConfigSetupDoc> channels = CollectionUtil.asList();
		MongoQueryBuilder<ChannelConfigSetupDoc> q = MongoQueryBuilder.collection(ChannelConfigSetupDoc.class)
				.page(pageNo, pageSize);
		if (ArgUtil.is(channelConfigId)) {
			q = q.whereIdSafe(channelConfigId);
		}
		if (ArgUtil.is(channelType)) {
			q.search("channelType", ArgUtil.parseAsString(channelType));
		}
		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		channels = commonMongoTemplate.find(q);

		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}
		if (ArgUtil.is(bootJxConfigService)) {
			model.addAllAttributes(bootJxConfigService.bootJxAttributesModel().cdnApp("test").cdnAEntry("dev")
					.preventUpgradeInsecureRequest().map());
		}
		model.addAttribute("APP_USER", "");
		model.addAttribute("APP_USER_NAME", "User");
		model.addAttribute("APP_USER_ROLE", "['GUEST']");
		model.addAttribute("channels", channels);

		boolean channelSelected = (channels.size() == 1);
		model.addAttribute("channelSelected", channelSelected);
		model.addAttribute("selectedChannelConfigId", Constants.BLANK);
		if (channelSelected) {
			model.addAttribute("selectedChannel", channels.get(0));
			model.addAttribute("selectedChannelConfigId", channels.get(0).getId());
		}
		return "app-setup-channel";

	}

	@ResponseBody
	@RequestMapping(value = "/ext/setup/channel", method = { RequestMethod.POST })
	public MapModel setupChannelSave(@RequestParam String channelConfigId, @RequestBody Map<String, Object> response)
			throws FileNotFoundException, IOException {
		MapModel returnVal = MapModel.createInstance();

		ChannelConfigSetupDoc setup = commonMongoTemplate.findById(channelConfigId, ChannelConfigSetupDoc.class);
		if (ArgUtil.is(setup)) {
			ChannelConfigTempDoc respDoc = new ChannelConfigTempDoc();
			respDoc.setChannelConfigId(channelConfigId);
			respDoc.setResp(response);
			respDoc.setChannelType(setup.getChannelType());
			commonMongoTemplate.save(respDoc);
			returnVal.put("id", respDoc.getId());
		}
		return returnVal;
	}

}
