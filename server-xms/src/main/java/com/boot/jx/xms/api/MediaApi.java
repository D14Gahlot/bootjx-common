package com.boot.jx.xms.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.ioutbound.MessageService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.MediaDoc;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Media", description = "API's to upload Media")
@Controller
public class MediaApi {

	@Autowired
	AWSFileStore fileStore;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@CrossOrigin(origins = "*")
	@ApiOperation(value = "Upload Media",
			notes = "This API can be used only to upload media,"
					+ "You will have to use Send Message} api to actial Send Message",
			hidden = true, authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v2/media/upload", method = { RequestMethod.POST })
	@ResponseBody
	public ApiResponse<Attachment, Object> uploadMedia(@RequestParam(required = false) String drive,
			@RequestParam(required = false) String bucket, @RequestParam(required = false) String name,
			@RequestParam(required = false) String caption, @RequestParam MultipartFile file) throws Exception {
		String drive_name = ArgUtil.nonEmpty(drive, "drive");
		String bucket_name = ArgUtil.nonEmpty(bucket, "bucket");
		String file_name = ArgUtil.nonEmpty(name, UUID.randomUUID().toString());
		String folder_path = ArgUtil.nonEmpty(Constants.BLANK, UUID.randomUUID().toString());

		CommonFile f = fileStore.upload1(file,
				String.format("%s/%s/%s/%s", AppContextUtil.getTenant(), drive_name, bucket_name, folder_path),
				file_name);

		Attachment media = new MediaDoc().mediaURL(f.getUrl()).mediaType(f.getFileType()).mediaCaption(caption);
		commonMongoTemplate.save(media, MediaDoc.COLLECTION_NAME);
		return ApiResponse.buildResult(media);
	}

}
