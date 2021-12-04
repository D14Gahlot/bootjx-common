package com.boot.jx.postman.model.ext;

import com.boot.jx.postman.model.Attachment;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InBoundMsgMedia extends CommonMsg {

    @Deprecated
    @ApiMockModelProperty(example = "/file/of/vdo", value = "absolute-filepath-on-coreapp", hidden = true)
    public String file;

    @ApiMockModelProperty(example = "2121212", value = "ID of the media",
	    notes = "Can be used to delete the media if stored locally on the client.")
    public String id;

    @ApiMockModelProperty(example = "http(s)://link-to-media-file-url", value = "link-to-audio-file")
    public String link;

    @JsonProperty("mime_type")
    @ApiMockModelProperty(example = "video/mp4", value = "Mime type of the media.")
    public String mimeType;

    @ApiMockModelProperty(example = "m3232kaoe4belrr", value = "checksum")
    public String sha256;

    @ApiMockModelProperty(example = "document-caption", value = "The provided caption for the media.", required = false)
    public String caption;

    @ApiMockModelProperty(example = "document-filename", value = "Filename on the sender's device.", required = false,
	    notes = "This will only be present in document media messages.")
    public String filename;

    @ApiMockModelProperty(example = "m3232kaoe4belrr", value = "Metadata pertaining to sticker media.", hidden = true)
    public Object metadata;

    public static InBoundMsgMedia from(Attachment attachment) {
	InBoundMsgMedia media = new InBoundMsgMedia();
	media.caption = attachment.getMediaCaption();
	media.filename = attachment.getMediaName();
	media.link = attachment.getMediaURL();
	media.mimeType = attachment.getMediaMimeType();
	return media;
    }

}