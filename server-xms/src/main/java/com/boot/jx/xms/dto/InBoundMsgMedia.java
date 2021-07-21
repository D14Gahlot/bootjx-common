package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InBoundMsgMedia {

    @Deprecated
    @ApiMockModelProperty(example = "/file/of/vdo", value = "absolute-filepath-on-coreapp", hidden = true)
    private String file;

    @ApiMockModelProperty(example = "2121212", value = "ID of the media",
	    notes = "Can be used to delete the media if stored locally on the client.")
    private String id;

    @ApiMockModelProperty(example = "http(s)://link-to-media-file-url", value = "link-to-audio-file")
    private String link;

    @JsonProperty("mime_type")
    @ApiMockModelProperty(example = "video/mp4", value = "Mime type of the media.")
    private String mimeType;

    @ApiMockModelProperty(example = "m3232kaoe4belrr", value = "checksum")
    private String sha256;

    @ApiMockModelProperty(example = "document-caption", value = "The provided caption for the media.", required = false)
    private String caption;

    @ApiMockModelProperty(example = "document-filename", value = "Filename on the sender's device.", required = false,
	    notes = "This will only be present in document media messages.")
    private String filename;

    @ApiMockModelProperty(example = "m3232kaoe4belrr", value = "Metadata pertaining to sticker media.", hidden = true)
    private Object metadata;

}