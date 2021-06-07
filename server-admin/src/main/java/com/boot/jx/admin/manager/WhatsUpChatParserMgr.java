package com.boot.jx.admin.manager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.admin.dto.ChatParserDto;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.model.Attachment;
import com.boot.utils.ArgUtil;
import com.boot.utils.CloseUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CommonDateTimeParser;

@Component
public class WhatsUpChatParserMgr {

	public static final SimpleDateFormat WA_DATE_FORMAT = new SimpleDateFormat("m/d/yy, hh:mm a");

	public ApiResponse<ChatSessionDTO, Map<String, Object>> getChats(MultipartFile file, String clientDate) {

		CommonDateTimeParser dtp = new CommonDateTimeParser().formatter("eee MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)")
				.date(clientDate).calculateZone().formatter("M/d/yy, h:mm a").withZone();

		List<ChatParserDto> list = this.getParseFileUsingRegExp(file);

		List<ChatSessionDTO> sessions = new ArrayList<ChatSessionDTO>();

		ChatSessionDTO session = null;
//		= new ChatSessionDTO();
//		session.setMessages(new ArrayList<ChatMessageDTO>());

		long lastMessageStamp = 0L;
		long sessionSeperatorLower = 1 * 24 * 60 * 60 * 1000L;
		long sessionSeperatorUpper = 30 * 24 * 60 * 60 * 1000L;
		long sessionMessageLimitLower = 20;

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
			session.getMessages().add(msg);
			session.setCloseSessionStamp(msg.getTimestamp());
		}
		return ApiResponse.buildResults(sessions, new HashMap<String, Object>());
	}

	public List<ChatParserDto> chatParser(MultipartFile file) {
		List<ChatParserDto> chatLst = new ArrayList<ChatParserDto>();

		InputStream is = null;
		BufferedReader br = null;

		char preString = '-';
		char searchString = ':';
		try {
			// FileReader fr = new FileReader("E:\\whatsup_chat\\chat.txt");
			// FileReader fr = new FileReader("E:\\whatsup_chat\\chat_with_m.txt");
			is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			String line = "";
			String buffer = "";
			String lastMember = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);

				if (!line.isEmpty()) {
					ChatParserDto dto = new ChatParserDto();
					String str[] = line.split("-");
					if (str != null && str.length >= 2) {
						String date = str[0];
						dto.setDate(date);

						String strMore = str[1];
						if (strMore != null) {
							String strMoreAr[] = strMore.split(":");
							if (strMoreAr != null && strMoreAr.length >= 2) {
								String auther = strMoreAr[0];
								String msg = strMoreAr[1];
								dto.setAuther(auther);
								dto.setMessage(msg);

							}
						}

					}

					if (dto != null && ArgUtil.is(dto.getDate())) {
						chatLst.add(dto);
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseUtil.close(br);
			CloseUtil.close(is);
		}
		return chatLst;
	}

	public List<ChatParserDto> getParseFileUsingRegExp(MultipartFile file) {
		// a. I would reference to my file
		// File wspLogFile = new File("data/wsp.log");
		// b. I would use the mechanism to read the file using BufferedReader
		// BufferedReader bufferedReader = new BufferedReader(new
		// FileReader(wspLogFile));

		InputStream is = null;
		BufferedReader br = null;
		List<ChatParserDto> chatLst = new ArrayList<ChatParserDto>();
		try {

			// FileReader fr = new FileReader("E:\\whatsup_chat\\mehery_chat.txt");

			is = file.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			String currLine = null;// This is the current line (like my cursor)

			// This will hold the data of the file in String format
			StringBuilder stringFormatter = new StringBuilder();
			boolean firstIterationDone = false;// The first line will always contains the format, so I will always
												// append it, from the second I will start making the checkings...

			String regex = "(\\d+/\\d+/\\d+, \\d+:\\d+\\d+ [A-Z]*) - (.*?): (.*)";

			String sCurrentLine;

			Pattern r = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL); // REGEX required for
																							// extracting data
			String lineSeparator = System.getProperty("line.separator");

			ChatParserDto lastMessage = null;
			while ((sCurrentLine = br.readLine()) != null) {

//				if (!firstIterationDone) {
//					stringFormatter.append(currLine);
//					firstIterationDone = true;
//				} else {
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
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CloseUtil.close(br);
			CloseUtil.close(is);
		}

		return chatLst;

	}

}
