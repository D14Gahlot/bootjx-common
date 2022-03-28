package com.boot.jx.bot.chakli;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.boot.jx.bot.BotController;
import com.boot.jx.bot.alex.CommonBotController;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.model.SafeKeyHashMap;
import com.boot.utils.ArgUtil;

@BotController(name = "chakli")
public class DefaultChakliController extends CommonBotController {

	protected static final String KEY_SELECT_LANGUAGE = "select-language";

	public static final String REPLY_ID = "reply_id";

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	PMEnvironment pmEnvironment;

	protected void resolveLanguage(String tmplCode) {
		reply(new OutboxMessage().template(tmplCode).put("name", context().contact().getName()));
		next(KEY_SELECT_LANGUAGE);
	}

	@SuppressWarnings({ "unchecked" })
	public Boolean checkValue(String tmplCode, String userInput) {
		Boolean booValue = false;
		String lang = ArgUtil.parseAsString(context().contact().getLang());
		Query query = new Query();
		query.addCriteria(Criteria.where("code").is(tmplCode).and("lang").is(lang));
		HSMTemplateDoc hsmTmpl = mongoTemplate.findOne(query, HSMTemplateDoc.class, "DICT_HSM_TEMPLATES");
		if (ArgUtil.is(hsmTmpl)) {
			Map<String, Object> options = hsmTmpl.getOptions();
			if (ArgUtil.is(options)) {
				List<Map<String, Object>> extTemCom = (List<Map<String, Object>>) options.get("buttons");
				booValue = extTemCom.stream().anyMatch(map -> map.containsValue(userInput));
			}
		}
		return booValue;
	}

	public String toReplyEnum(InboxMessage inboxMessage) {
		String codeValue = inboxMessage.form().get(REPLY_ID) == null ? inboxMessage.getMessage()
				: inboxMessage.form().get(REPLY_ID).toString();
		if (ArgUtil.is(codeValue)) {
			codeValue = codeValue.toLowerCase().trim();
		}
		System.out.println("codeValue :" + codeValue);
		return codeValue;
	}

	public boolean timeCheck() {
		SafeKeyHashMap<Object> globalVars = pmEnvironment.local().globalVars();
		boolean officeTimeFlag = globalVars.keyEntry("office_time_msg").asBoolean();
		boolean isNowInRange = false;
		if (officeTimeFlag) {
			String startTime = globalVars.keyEntry("office_start_time").asString();
			String endTime = globalVars.keyEntry("office_start_time").asString();

			try {
				LocalTime now = LocalTime.now(ZoneId.of("Asia/Kuwait"));
				String isoTime = now.format(DateTimeFormatter.ISO_TIME);
				LocalTime currTime = LocalTime.parse(isoTime, DateTimeFormatter.ISO_TIME);
				LocalTime start = LocalTime.of(Integer.valueOf(startTime), 0);
				LocalTime stop = LocalTime.of(Integer.valueOf(endTime), 0);

				isNowInRange = (!currTime.isBefore(start)) && currTime.isBefore(stop);

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			isNowInRange = true;
		}
		return isNowInRange;
	}

}
