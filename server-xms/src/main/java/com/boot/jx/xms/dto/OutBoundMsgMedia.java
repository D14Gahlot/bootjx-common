package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class OutBoundMsgMedia {

    @ApiMockModelProperty(example = "5do9756xbso34578", required = false, value = "Media Id",
    	notes = "Required when type is audio, document, image, sticker, or video and you are not using a link."
    		+ "\n The media object ID. This is returned when the media is successfully uploaded to the "
    		+ "WhatsApp Business API client via the media endpoint.\n"
    		+ "\n Do not use this field when message type is set to text.")
    public String id;

    @ApiMockModelProperty(example = "http(s)://the-url", required = false, value = "Public URL of Media file",
    	notes = "Required when type is audio, document, image, sticker, or video and you are not using an uploaded media ID."
    		+ "\n The protocol and URL of the media to be sent. Use only with HTTP/HTTPS URLs.\n" + "\n"
    		+ "\n Do not use this field when message type is set to text.")
    public String link;

    @ApiMockModelProperty(example = "your-video-caption", required = false, value = "your-media-caption",
    	notes = "Describes the specified document, image, or video media.\n"
    		+ "\n Do not use with audio or sticker media.")
    public String caption;

    @ApiMockModelProperty(example = "your-video-caption", required = false, value = "your-document-filename",
    	notes = "Describes the specified document, image, or video media.\n"
    		+ "Describes the filename for the specific document. Use only with document media.")
    public String filename;
}