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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.ChatParserDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.MapModel;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CloseUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CommonDateTimeParser;
import com.boot.utils.Constants;

@Component
public class ChatParserAndImportor {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	MongoTemplate mongoTemplate;

	public ApiResponse<ChatSessionDTO, Map<String, Object>> importChat(
			ApiResponse<ChatSessionDTO, Map<String, Object>> request) {

		MapModel meta = MapModel.from(request.getMeta());
		String contactMobile = meta.getString("contactMobile");
		String contactName = meta.getString("contactName");
		String contact = meta.getString("contact");
		String lane = meta.getString("lane");
		ContactType contactType = meta.getAsEnum(lane, ContactType.class);
		String contactId = PostManUtil.createContactId(contactType, contactMobile, lane);

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

		for (ChatSessionDTO session : request.getResults()) {

			ChatSessionDoc sessionDoc = sessionStore.createSession(chatContactDoc);

			List<MessageDoc> messageDocs = new ArrayList<MessageDoc>();
			for (ChatMessageDTO message : session.getMessages()) {
				MessageDoc messageDoc = new MessageDoc();
				if (contact.equals(message.getSender())) {
					messageDoc.setType("Ii");
				} else if (contact.equals(message.getSender())) {
					messageDoc.setType("Oi");
					messageDoc.setStatus(Message.Status.SENT.toString());
				}
				messageDoc.setMessage(message.getText());
				messageDoc.setTimestamp(message.getTimestamp());
				messageDoc.setAttachments(message.getAttachments());
				messageDoc.setContactId(sessionDoc.getContactId());
				messageDoc.setSessionId(sessionDoc.getSessionId());

				messageDocs.add(messageDoc);
			}
			mongoTemplate.insertAll(messageDocs);
		}

		return request;
	}

	public ApiResponse<ChatSessionDTO, Map<String, Object>> getChats(MultipartFile file, ContactType contactType,
			String clientDate, String format) {

		CommonDateTimeParser dtp = new CommonDateTimeParser()
				.formatter(ArgUtil.nonEmpty(format, "ccc LLL dd yyyy HH:mm:ss 'GMT'Z (zzzz)")).date(clientDate)
				.calculateZone().formatter("M/d/yy, h:mm a").withZone();

		List<ChatSessionDTO> sessions = new ArrayList<ChatSessionDTO>();
		Map<String, Object> meta = new HashMap<String, Object>();

		List<ChatParserDto> list = this.getParseFileUsingRegExp(file, meta);

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

		return ApiResponse.buildResults(sessions, meta);
	}

	public List<ChatParserDto> getParseFileUsingRegExp(MultipartFile file, Map<String, Object> meta) {
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
			meta.put("file-md5", hashtext);
			meta.put("file-name", file.getOriginalFilename());
			meta.put("file-size", file.getSize());
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
