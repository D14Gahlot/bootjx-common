package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.utils.ArgUtil;

@Document(collection = "DICT_MEDIA_RESP")
@TypeAlias("MediaReply")
public class MediaReply implements Serializable {

	private static final long serialVersionUID = 7942286016346691701L;

	@Id
	private CompositeKey id;

	@Indexed(unique = true)
	private String uniqueId;

	private String message;
	private String media;
	private String image;
	private String video;

	public CompositeKey id() {
		if (!ArgUtil.is(this.id)) {
			this.id = new CompositeKey();
		}
		return id;
	}

	public static class CompositeKey implements Serializable {
		private static final long serialVersionUID = -2593141269922970618L;
		private String subject;
		private String gallery;

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getGallery() {
			return gallery;
		}

		public void setGallery(String gallery) {
			this.gallery = gallery;
		}

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public CompositeKey getId() {
		return id;
	}

	public void setId(CompositeKey id) {
		this.id = id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getVideo() {
		return video;
	}

	public void setVideo(String video) {
		this.video = video;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

}
