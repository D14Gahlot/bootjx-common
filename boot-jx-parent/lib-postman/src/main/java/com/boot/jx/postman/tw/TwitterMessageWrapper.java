/*
 * package com.boot.jx.postman.tw;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import com.boot.jx.dict.ContactType; import
 * com.boot.jx.postman.model.InboxMessage; import
 * com.boot.jx.postman.model.WAMessage.Channel;
 * 
 * import twitter4j.DirectMessage; import twitter4j.DirectMessageList;
 * 
 * public class TwitterMessageWrapper {
 * 
 * private static
 * 
 * 
 * public static TwitterMessageResponse messageConverter(DirectMessageList dms)
 * { TwitterMessageResponse tmr = new TwitterMessageResponse();
 * List<TwitterMessage> lstOfMsg = new ArrayList<TwitterMessage>();
 * List<InboxMessage> inboxMsg = new ArrayList<InboxMessage>();
 * 
 * for(DirectMessage dm: dms ) { TwitterMessage tm = new TwitterMessage();
 * InboxMessage ibm = new InboxMessage(); tm.setId(dm.getId());
 * tm.setText(dm.getText()); tm.setSenderId(dm.getSenderId());
 * tm.setRecipientId(dm.getRecipientId());
 * 
 * ibm.setMessageId(String.valueOf(dm.getId())); ibm.setMessage(dm.getText());
 * ibm.setFrom(String.valueOf(dm.getSenderId()));
 * ibm.setTo(String.valueOf(dm.getRecipientId()));
 * ibm.setChannel(Channel.GUPSHUP.toString());
 * ibm.setContactType(ContactType.TWITTER);
 * 
 * inboxMsg.add(ibm);
 * 
 * //lstOfMsg.add(tm); } tmr.setLstOfMsg(lstOfMsg); tmr.setInboxMsg(inboxMsg);
 * return tmr; }
 * 
 * }
 */