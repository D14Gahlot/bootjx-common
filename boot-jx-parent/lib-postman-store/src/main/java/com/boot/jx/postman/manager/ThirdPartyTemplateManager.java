package com.boot.jx.postman.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360Template;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
public class ThirdPartyTemplateManager {

    @Autowired
    private WA360Client wa360Client;

    @Autowired
    CommonMongoTemplate commonMongoTemplate;

    public void refreshWA360Templates(ChannelConfig channelConfig) {
	MapModel resp = wa360Client.fetchTemplates(channelConfig);

	List<WA360Template> wabaTemplates = resp.keyEntry("waba_templates").asList(WA360Template.class);

	for (WA360Template wa360Template : wabaTemplates) {
	    String id = String.format("%s/%s/%s", channelConfig.getChannelId(), wa360Template.getName(),
		    wa360Template.getLang());
	    HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(id, HSMTemplate3rdParty.class);
	    if (!ArgUtil.is(thirdPartyTemplate)) {
		thirdPartyTemplate = new HSMTemplate3rdParty();
		thirdPartyTemplate.setId(id);
	    }
	    thirdPartyTemplate.setChannelId(id);
	    thirdPartyTemplate.setWa360Template(wa360Template);
	    commonMongoTemplate.save(thirdPartyTemplate);
	}
    }

    public List<HSMTemplate3rdParty> getTemplates(ChannelConfig channelConfig) {
	return commonMongoTemplate.find(new CommonMongoQueryBuilder().where("channelId", channelConfig.getChannelId()),
		HSMTemplate3rdParty.class);
    }

    public HSMTemplate3rdParty link(String thirdPartyTemplateId, String hsmTemplateId) {
	HSMTemplate3rdParty thirdPartyTemplate = commonMongoTemplate.findById(thirdPartyTemplateId,
		HSMTemplate3rdParty.class);
	thirdPartyTemplate.setHsmTemplateId(hsmTemplateId);
	commonMongoTemplate.save(thirdPartyTemplate);
	return thirdPartyTemplate;
    }

}
