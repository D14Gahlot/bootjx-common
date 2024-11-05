package com.boot.jx.xms.api;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.outbound.MessageService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.outbound.OutBoundReciept;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.model.MapModel;
import com.boot.utils.JsonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Outbound Messages", description = "API's to send OutBound Messages")
@RestController
public class MoengageApi {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MoengageApi.class);

	@Autowired
	private MessageService messageService;
	
	@Autowired
	private PMEnvironment pmEnvironment;

	@CrossOrigin(origins = "*")
	@ApiOperation(value = "Send Message", notes = "${swagger.MoengageApi.sendMessage.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/moengage/v1/message/send", method = { RequestMethod.POST })
	public ApiResponse<OutBoundReciept, Object> sendMessage(@RequestParam String channelId ,@RequestBody MapModel mapModel) {
		
		LOGGER.info("MoengageApi { sendMessage }"+channelId+"\n MAP"+JsonUtil.toJson(mapModel));
		//Map<String, Object> messageMap = mapModel.pathEntry("payload").asMap();
		
		Map<String, Object> messageMap = mapModel.toMap();
		messageMap = JsonUtil.toJsonMap(messageMap);
		LOGGER.info("MoengageApi {messageMap  ===}"+messageMap);
		OutboxMessage outBoxmessage=new OutboxMessage();
		//outBoxmessage.setRawMessageFormat(messageMap);
		 outBoxmessage =messageWrapper(messageMap);
		LOGGER.info("MoengageApi { outBoxmessage }"+JsonUtil.toJson(outBoxmessage));
		return ApiResponse.buildResult(messageService.send(channelId,outBoxmessage));
	}

	
	
	private OutboxMessage messageWrapper(Map<String, Object> map) {
		//OutBoundMsg outBoundMsg = new OutBoundMsg();
		OutboxMessage outboxMessage=new OutboxMessage();
		ContactMeta contactmeta = new ContactMeta();
		LOGGER.info("messageWrapper ---"+map);
		MapModel botreply = MapModel.from(map);
		//MapPathEntry text = botreply.keyEntry("text");
		//MapPathEntry quickReplyEntries = botreply.keyEntry("quick_replies");
		//MapPathEntry attachment = botreply.keyEntry("attachment");
		/** setting up the template **/
		//MapPathEntry template = botreply.keyEntry("template");
	
		//LOGGER.info("messageWrapper ---template "+template);
		/** setting the contact **/
		String to =(String)botreply.get("to");
		contactmeta.phone(to);
		outboxMessage.setContact(contactmeta);
		/** end contact **/
		// set template name 
		//JsonUtil.toJsonMap(map)
		outboxMessage.setRawMessageFormat(map);
		
	
		
	/*	if (attachment.exists()) {
			if (attachment.keyEntry("type").is("template")) {
				MapPathEntry payload = attachment.keyEntry("payload");
				if (payload.keyEntry("template_type").is("button")) {
					text = payload.keyEntry("text");
					if (text.exists()) {
						outboxMessage.message(text.asString());
//						OutBoundMsgText outBoundMsgtext = new OutBoundMsgText();
//						outBoundMsgtext.setBody(text.asString());
//						outBoundMsg.setText(outBoundMsgtext);
						
						
					}
					MapPathEntry buttonEntries = payload.keyEntry("buttons");
					if (buttonEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> button : buttonEntries.asListOfMap()) {
							MapModel buttonEntry = MapModel.from(button);
							buttons.add(new TmplElement() //
									.type(buttonEntry.keyEntry("type").asString())//
									.code(buttonEntry.keyEntry("payload").asString()) //
									.label(buttonEntry.keyEntry("title").asString()));
						}
						outboxMessage.buttons(buttons);
						
						
					
						
					}

				} else if (payload.keyEntry("template_type").is("list")) {
					MapPathEntry elementEntries = payload.keyEntry("elements");
					if (elementEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> element : elementEntries.asListOfMap()) {
							MapModel elementEntry = MapModel.from(element);

							outboxMessage.option("list_option_title", elementEntry.keyEntry("title").asString("Menu"));

							MapPathEntry subtitle = elementEntry.keyEntry("subtitle");
							if (subtitle.exists()) {
								outboxMessage.message(text.asString());
							}

							MapPathEntry buttonEntries = elementEntry.keyEntry("buttons");
							if (buttonEntries.exists()) {
								for (Map<String, Object> button : buttonEntries.asListOfMap()) {
									MapModel buttonEntry = MapModel.from(button);
									buttons.add(new TmplElement() //
											.type(buttonEntry.keyEntry("type").asString())//
											.code(buttonEntry.keyEntry("payload").asString()) //
											.label(buttonEntry.keyEntry("title").asString()));
								}
							}
						}
						outboxMessage.buttons(buttons);
					}
				} else if (payload.keyEntry("template_type").is("generic")) {
					MapPathEntry elementEntries = payload.keyEntry("elements");
					if (elementEntries.exists()) {
						List<TmplElement> buttons = TmplElement.list();
						for (Map<String, Object> element : elementEntries.asListOfMap()) {
							MapModel elementEntry = MapModel.from(element);

							outboxMessage.option("list_option_title", elementEntry.keyEntry("title").asString("Menu"));

							MapPathEntry subtitle = elementEntry.keyEntry("subtitle");
							MapPathEntry image = elementEntry.keyEntry("image_url");
							if (image.exists()) {
								Attachment attch = new Attachment().mediaURL(image.asString())
										.mediaType(FileType.IMAGE);
								if (subtitle.exists()) {
									attch.mediaCaption(subtitle.asString());
								}
								outboxMessage.attachment(attch);
							} else if (subtitle.exists()) {
								outboxMessage.message(text.asString());
							}

							MapPathEntry buttonEntries = elementEntry.keyEntry("buttons");
							if (buttonEntries.exists()) {
								for (Map<String, Object> button : buttonEntries.asListOfMap()) {
									MapModel buttonEntry = MapModel.from(button);
									buttons.add(new TmplElement() //
											.type(buttonEntry.keyEntry("type").asString())//
											.code(buttonEntry.keyEntry("payload").asString()) //
											.label(buttonEntry.keyEntry("title").asString()));
								}
							}

						}
						outboxMessage.buttons(buttons);
					}
				}
			}
			return outboxMessage;
		} else if (quickReplyEntries.exists()) {
			List<TmplElement> buttons = TmplElement.list();
			for (Map<String, Object> quickReply : quickReplyEntries.asListOfMap()) {
				MapModel quickReplyEntry = MapModel.from(quickReply);
				buttons.add(new TmplElement() //
						.type(quickReplyEntry.keyEntry("content_type").asString())//
						.code(quickReplyEntry.keyEntry("payload").asString()) //
						.label(quickReplyEntry.keyEntry("title").asString()));
			}
			outboxMessage.buttons(buttons);
			if (text.exists()) {
				outboxMessage.message(text.asString());
			}
			return outboxMessage;
		} else if (text.exists()) {
			 return outboxMessage.message(text.asString());
		}else if(template.exists()) {
			
		}*/
		return outboxMessage;
	}

	
	
}
