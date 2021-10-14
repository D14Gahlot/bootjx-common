package com.boot.jx.admin.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.ChatParserDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.ImportChatSessionDoc;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoQueryBuilder.CommonMongoCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.MessageDocWA;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.gupshup.GupShupConfigDetails;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CloseUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CommonDateTimeParser;
import com.boot.utils.Constants;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.UniqueID;

@Component
public class ChatParserAndImportor {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private CommonMongoTemplate commpnMongoTemplate;

	@Autowired
	private AuditDetailProvider auditDetailProvider;

	public ApiResponse<ImportChatSessionDoc, Object> trashChat(ImportChatSessionDoc doc) {
		ImportChatSessionDoc docs = commpnMongoTemplate.findByIdString(doc.getId(), ImportChatSessionDoc.class);

		if (ArgUtil.is(docs)) {
			for (String sessionId : docs.getSessions()) {
				ChatSessionDoc session = new ChatSessionDoc();
				session.setSessionId(sessionId);
				session.setContactType(ArgUtil.parseAsString(docs.getContactType()));
				sessionStore.deleteSession(session);
			}
			docs.setStatus("DELETED");
			commpnMongoTemplate.save(docs);
		}
		return ApiResponse.buildResults(commpnMongoTemplate.findAll(ImportChatSessionDoc.class));
	}

	public ApiResponse<ChatSessionDTO, Map<String, Object>> importChat(
			ApiResponse<ChatSessionDTO, Map<String, Object>> request) {

		MapModel meta = MapModel.from(request.getMeta());
		String contactMobile = meta.getString("contactMobile");
		String contactName = meta.getString("contactName");
		String contact = meta.getString("contact");
		String sender = meta.getString("sender");
		String lane = meta.getString("lane");
		ContactType contactType = meta.getAsEnum("contactType", ContactType.class);
		String contactId = PostManUtil.createContactId(contactType, contactMobile, lane);

		ImportChatSessionDoc importDetails = meta.getAs("importDetails", ImportChatSessionDoc.class);

		ChatContactDoc chatContactDoc = sessionStore.getContact(contactId);

		if (ArgUtil.isEmpty(chatContactDoc)) {
			chatContactDoc = new ChatContactDoc();
			chatContactDoc.setContactId(contactId);
			chatContactDoc.setContactType(ArgUtil.parseAsString(contactType));
			chatContactDoc.setChannelType("IMPORT");
			chatContactDoc.setLane(lane);
			chatContactDoc.setCsid(contactMobile);
			chatContactDoc.setName(contactName);
			sessionStore.save(chatContactDoc);
		}

		List<String> sessionIds = new ArrayList<String>();
		for (ChatSessionDTO session : request.getResults()) {

			ChatSessionDoc chatSessionDoc = EntityDtoUtil.dtoToEntity(session, new ChatSessionDoc());
			chatSessionDoc.setContactId(chatContactDoc.getContactId());
			chatSessionDoc.setContactType(chatContactDoc.getContactType());
			chatSessionDoc.setChannel(chatContactDoc.getChannelType());
			chatSessionDoc.setLane(chatContactDoc.getLane());

			chatSessionDoc.setResolved(true);
			chatSessionDoc.setResolveSessionStamp(chatSessionDoc.getCloseSessionStamp());
			chatSessionDoc.setCloseSessionStamp(chatSessionDoc.getCloseSessionStamp());
			chatSessionDoc.setExpired(true);
			chatSessionDoc.setAssignedToAgent(sender);
			chatSessionDoc.setContactName(contactName);

			// chatSessionDoc.setStartSessionStamp(session.getStartSessionStamp());

			sessionStore.save(chatSessionDoc);

			long getFistResponseStamp = 0L;
			long getLastInComingStamp = 0L;
			long getLastResponseStamp = 0L;

			List<MessageDoc> messageDocs = new ArrayList<MessageDoc>();
			for (ChatMessageDTO message : session.getMessages()) {
				MessageDocWA messageDoc = new MessageDocWA();
				if (contact.equals(message.getSender())) {
					messageDoc.setType("Ii");
					getLastInComingStamp = Math.max(message.getTimestamp(), getLastInComingStamp);

				} else if (sender.equals(message.getSender())) {
					messageDoc.setType("Oi");
					messageDoc.setStatus(Message.Status.SENT.toString());
					if (getFistResponseStamp == 0L) {
						getFistResponseStamp = message.getTimestamp();
					}
					getFistResponseStamp = Math.min(message.getTimestamp(), getFistResponseStamp);
					getLastResponseStamp = Math.max(message.getTimestamp(), getLastResponseStamp);
					messageDoc.setAgent(message.getSender());
				}
				messageDoc.setMessage(message.getText());
				messageDoc.setTimestamp(message.getTimestamp());
				messageDoc.setAttachments(message.getAttachments());
				messageDoc.setContactId(chatSessionDoc.getContactId());
				messageDoc.setSessionId(chatSessionDoc.getSessionId());

				messageDocs.add(messageDoc);
			}
			mongoTemplate.insertAll(messageDocs);

			chatSessionDoc.setFistResponseStamp(getFistResponseStamp);
			chatSessionDoc.setLastInComingStamp(getLastInComingStamp);
			chatSessionDoc.setLastResponseStamp(getLastResponseStamp);

			sessionStore.save(chatSessionDoc);
			sessionIds.add(chatSessionDoc.getSessionId());
		}

		importDetails.setSessions(sessionIds);
		importDetails.setContact(contact);
		importDetails.setContactId(contactId);
		importDetails.setContactMobile(contactMobile);
		importDetails.setContactName(contactName);
		importDetails.setContactType(contactType);
		importDetails.setLane(lane);
		importDetails.setSender(sender);
		importDetails.setStatus("COMPLETED");
		mongoTemplate.save(importDetails);

		return request;
	}

	public ApiResponse<ChatSessionDTO, Map<String, Object>> getChats(MultipartFile file, ContactType contactType,
			String clientDate, String format) {

		CommonDateTimeParser dtp = new CommonDateTimeParser()
				.formatter(ArgUtil.nonEmpty(format, "ccc LLL dd yyyy HH:mm:ss 'GMT'Z (zzzz)")).date(clientDate)
				.calculateZone().formatter("M/d/yy, h:mm a").withZone();

		List<ChatSessionDTO> sessions = new ArrayList<ChatSessionDTO>();
		Map<String, Object> meta = new HashMap<String, Object>();

		ImportChatSessionDoc importChatSession = new ImportChatSessionDoc();
		importChatSession.setContactType(contactType);
		importChatSession.setCreatedBy(auditDetailProvider.getAuditUser());
		importChatSession.setCreatedStamp(System.currentTimeMillis());

		List<ChatParserDto> list = this.getParseFileUsingRegExp(file, importChatSession);

		ChatSessionDTO session = null;

		long lastMessageStamp = 0L;
		long sessionSeperatorLower = 1 * 24 * 60 * 60 * 1000L;
		long sessionSeperatorUpper = 30 * 24 * 60 * 60 * 1000L;
		long sessionMessageLimitLower = 30;

		String senderA = null;
		String senderB = null;

		for (ChatParserDto item : list) {

			ChatMessageDTO msg = new ChatMessageDTO();
			msg.setSender(item.getAuther());
			msg.setTimestamp(dtp.date(item.getDate().trim()).toUTCTimeStamp());
			if ("<Media omitted>".equalsIgnoreCase(item.getMessage())) {
				msg.setAttachments(CollectionUtil.getList(new Attachment().mediaCaption(item.getMessage())));
			} else if ("Missed voice call".equalsIgnoreCase(item.getMessage())
					|| "Missed video call".equalsIgnoreCase(item.getMessage())
					|| "Missed group voice call".equalsIgnoreCase(item.getMessage())) {
				continue;
			} else {
				msg.setText(item.getMessage());
			}
			long diffStamp = msg.getTimestamp() - lastMessageStamp;
			if (ArgUtil.isEmpty(session) // Current Session is Empty
					|| diffStamp > sessionSeperatorUpper // Its more than upper limit
					|| (diffStamp > sessionSeperatorLower // Its more than lower limit
							&& session.getMessages().size() > sessionMessageLimitLower // Current session has
																						// More message
					)) {
				session = new ChatSessionDTO();
				session.setMessages(new ArrayList<ChatMessageDTO>());
				session.setStartSessionStamp(msg.getTimestamp());
				sessions.add(session);
			}
			lastMessageStamp = msg.getTimestamp();
			if (ArgUtil.isEmpty(senderA)) {
				senderA = msg.getSender();
			} else if (ArgUtil.isEmpty(senderB) && !senderA.equalsIgnoreCase(msg.getSender())) {
				senderB = msg.getSender();
			}

			session.getMessages().add(msg);
			session.setCloseSessionStamp(msg.getTimestamp());
		}

		meta.put("senderA", ArgUtil.parseAsString(senderA, Constants.BLANK));
		meta.put("senderB", ArgUtil.parseAsString(senderB, Constants.BLANK));
		meta.put("contactType", contactType);
		meta.put("timezone", dtp.getZone());

		List<String> lanes = new ArrayList<String>();
		if (ContactType.WHATSAPP.equals(contactType)) {
			for (Entry<String, GupShupConfigDetails> conifg : environment.config().gupshup().entrySet()) {
				lanes.add(conifg.getValue().getNumber());
			}

		}
		meta.put("lanes", lanes);

		importChatSession.setCountSessions(sessions.size());
		importChatSession.setCountMessages(list.size());
		importChatSession.setTimezone(dtp.getZone().toString());
		importChatSession.setStatus("CREATED");
		mongoTemplate.save(importChatSession);

		meta.put("importDetails", importChatSession);

		CommonMongoQueryBuilder qb = new CommonMongoQueryBuilder().with(
				CommonMongoCriteria.where("fileMD5").is(importChatSession.getFileMD5()).and("status").is("COMPLETED"));
		List<ImportChatSessionDoc> duplicates = mongoTemplate.find(qb.getQuery(), ImportChatSessionDoc.class);
		if (ArgUtil.is(duplicates)) {
			meta.put("duplicates", importChatSession);
		}

		return ApiResponse.buildResults(sessions, meta);
	}

	public List<ChatParserDto> getParseFileUsingRegExp(MultipartFile file, ImportChatSessionDoc importChatSessionDoc) {
		InputStream is = null;
		BufferedReader br = null;
		DigestInputStream dis = null;
		List<ChatParserDto> chatLst = new ArrayList<ChatParserDto>();
		try {

			MessageDigest md = MessageDigest.getInstance("MD5");

			is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			dis = new DigestInputStream(is, md);

			String regex = "(\\d+/\\d+/\\d+, \\d+:\\d+\\d+ [A-Z]*) - (.*?): (.*)";

			String sCurrentLine;

			Pattern r = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL); // REGEX required for
																							// extracting data
			String lineSeparator = System.getProperty("line.separator");

			ChatParserDto lastMessage = null;
			while ((sCurrentLine = br.readLine()) != null) {
				Matcher m = r.matcher(sCurrentLine);
				if (m.find()) {
					// System.out.println("======" + sCurrentLine);
					// New Message
					lastMessage = new ChatParserDto();
					String date = m.group(1);
					String auther = m.group(2);
					String msg = m.group(3);
					lastMessage.setDate(date);
					lastMessage.setAuther(auther);

					lastMessage.setMessage(msg);
					// System.out.println("Message: " + m.group(4) );
					if (lastMessage != null && ArgUtil.is(lastMessage.getDate())) {
						chatLst.add(lastMessage);
					}
				} else if (ArgUtil.is(lastMessage)) {
					// System.out.println(" " + sCurrentLine);
					lastMessage.setMessage(lastMessage.getMessage() + lineSeparator + sCurrentLine);
				}
			}

			byte[] digest = dis.getMessageDigest().digest();

			BigInteger bigInt = new BigInteger(1, digest);
			String hashtext = bigInt.toString(16);
			// Pad it to get full 32 chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			importChatSessionDoc.setFileMD5(hashtext);
			importChatSessionDoc.setFileName(file.getOriginalFilename());
			importChatSessionDoc.setFileSize(ArgUtil.parseAsString(file.getSize()));
			importChatSessionDoc.setFileId(UniqueID.generateString62());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseUtil.close(br);
			CloseUtil.close(is);
			CloseUtil.close(dis);
		}

		return chatLst;

	}

}
