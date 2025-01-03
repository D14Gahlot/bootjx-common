package com.boot.jx.admin.api;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.KnowBase;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.doc.QuickLocation;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.QuickSkill;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.store.QuickStore;
import com.boot.utils.ArgUtil;

@RestController
public class TmplQuickController {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	@Autowired
	private AuditDetailProvider auditDetailProvider;

	@Autowired
	private QuickStore quickStore;

	// QuickReply
	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.POST })
	public List<QuickReply> mapSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		QuickReply sr = new QuickReply();
		sr.setTemplate(template);
		sr.setMessage(message);
		mongoTemplate.save(sr);
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<QuickReply> listSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.GET })
	public ApiResponse<QuickReply, Object> listQuickReply() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class));
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.DELETE })
	public ApiResponse<QuickReply, Object> deleteQuickReply(@RequestParam String id) {
		QuickReply qr = mongoTemplate.removeAndAudit(id, QuickReply.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class)).data(qr).message("QuickReply deleted");
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.POST })
	public ApiResponse<QuickReply, Object> createQuickReply(@RequestBody QuickReply req) {

		QuickReply newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new QuickReply());

		newVersion.setCategory(req.getCategory());
		newVersion.setTitle(req.getTitle());
		newVersion.setTemplate(req.getTemplate());

		auditDetailProvider.auditCreate(newVersion);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class)).data(newVersion)
				.message("QuickReply Saved");
	}

	// QuickAction
	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.GET })
	public ApiResponse<QuickAction, Object> listQuickAction() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class));
	}

	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.DELETE })
	public ApiResponse<QuickAction, Object> deleteQuickAction(@RequestParam String id) {
		QuickAction qr = mongoTemplate.removeAndAudit(id, QuickAction.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class)).data(qr)
				.message("QuickAction deleted");
	}

	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.POST })
	public ApiResponse<QuickAction, Object> createQuickAction(@RequestBody QuickAction req) {
		QuickAction newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new QuickAction());
		newVersion.setCategory(req.getCategory());
		newVersion.setTitle(req.getTitle());
		newVersion.setAction(req.getCode());
		auditDetailProvider.auditCreate(newVersion);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class)).data(newVersion)
				.message("QuickAction Saved");
	}

	// QuickMedia
	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.GET })
	public ApiResponse<QuickMedia, Object> listQuickMedia() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class));
	}

	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.DELETE })
	public ApiResponse<QuickMedia, Object> deleteQuickMedia(@RequestParam String id) {
		QuickMedia qr = mongoTemplate.removeAndAudit(id, QuickMedia.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class)).data(qr)
				.message("Quick Media deleted");
	}

	@Autowired
	AWSFileStore fileStore;

	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.POST })
	public ApiResponse<QuickMedia, Object> createQuickMedia(@RequestParam(required = false) String id,
			@RequestParam String category, @RequestParam String title, @RequestParam(required = false) String url,
			@RequestParam String code, @RequestParam(name = "file", required = false) MultipartFile file,
			@RequestParam(required = false) FileType fileType, @RequestParam(required = false) FileFormat fileFormat) {

		if (ArgUtil.isEmpty(url) && ArgUtil.is(file)) {
			CommonFile commonfile = fileStore.upload1(file,
					String.format("%s/quickmedia/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
					file.getOriginalFilename());
			fileType = commonfile.getFileType();
			fileFormat = commonfile.getFileFormat();
			url = commonfile.getUrl();
		} else if (ArgUtil.isEmpty(url)) {
			throw new IllegalStateException("Cannot upload empty file");
		}

		QuickMedia newVersion = mongoTemplate.findByIdOrDefault(id, new QuickMedia());

		newVersion.setTitle(title);
		newVersion.setCategory(category);
		newVersion.setUrl(url);
		newVersion.setCode(code);
		newVersion.setType(ArgUtil.parseAsString(fileType));
		if (ArgUtil.is(fileFormat)) {
			newVersion.setFormat(fileFormat.name());
			newVersion.setMimeType(fileFormat.getContentType());
		}
		auditDetailProvider.auditCreate(newVersion);
		mongoTemplate.save(newVersion);

		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class)).data(newVersion)
				.message("Quick Media Saved");
	}

	// QuickLabel
	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.GET })
	public ApiResponse<QuickLabel, Object> listQuickLabels() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class));
	}

	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.DELETE })
	public ApiResponse<QuickLabel, Object> deleteQuickLabels(@RequestParam String id) {
		QuickLabel qr = mongoTemplate.removeAndAudit(id, QuickLabel.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class)).data(qr).message("QuickLabel deleted");
	}

	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.POST })
	public ApiResponse<QuickLabel, Object> createQuickLabels(@RequestBody QuickLabel req) {
		QuickLabel newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new QuickLabel());
		newVersion.setCategory(req.getCategory());
		newVersion.setTitle(req.getTitle());
		newVersion.setCode(req.getCode());
		auditDetailProvider.auditCreate(newVersion);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class)).data(newVersion)
				.message("QuickLabel Saved");
	}

	/** for adding quick Tag category e.g flight,train ,etc */

	// QuickLabel
	@RequestMapping(value = "/api/tmpl/quicktags", method = { RequestMethod.GET })
	public ApiResponse<QuickTag, Object> listQuickTagCategory() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickTag.class));
	}

	@RequestMapping(value = "/api/tmpl/quicktags", method = { RequestMethod.DELETE })
	public ApiResponse<QuickTag, Object> deleteQuickTagCategory(@RequestParam String id) {
		QuickTag qr = mongoTemplate.removeAndAudit(id, QuickTag.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickTag.class)).data(qr).message("QuickTag deleted");
	}

	@RequestMapping(value = "/api/tmpl/quicktags", method = { RequestMethod.POST })
	public ApiResponse<QuickTag, Object> createQuickTagCategory(@RequestBody QuickTag req) {
		QuickTag quickTag = mongoTemplate.findByIdOrDefault(req.getId(), new QuickTag());
		quickTag.setCategory(req.getCategory());
		quickTag.setTitle(req.getTitle());
		quickTag.setCode(req.getCode());
		mongoTemplate.saveAndAudit(quickTag, ArgUtil.is(quickTag.getId()));
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickTag.class)).data(quickTag).message("QuickTag Saved");
	}

	// QuickLabel
	@RequestMapping(value = "/api/tmpl/quickskills", method = { RequestMethod.GET })
	public ApiResponse<QuickSkill, Object> listQuickSkills() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickSkill.class));
	}

	@RequestMapping(value = "/api/tmpl/quickskills", method = { RequestMethod.DELETE })
	public ApiResponse<QuickSkill, Object> deleteQuickSkills(@RequestParam String id) {
		QuickSkill qr = mongoTemplate.removeAndAudit(id, QuickSkill.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickSkill.class)).data(qr).message("QuickSkill deleted");
	}

	@RequestMapping(value = "/api/tmpl/quickskills", method = { RequestMethod.POST })
	public ApiResponse<QuickSkill, Object> createQuickSkills(@RequestBody QuickSkill req) {
		QuickSkill quickTag = mongoTemplate.findByIdOrDefault(req.getId(), new QuickSkill());
		quickTag.setCategory(req.getCategory());
		quickTag.setTitle(req.getTitle());
		quickTag.setCode(req.getCode());
		mongoTemplate.saveAndAudit(quickTag, ArgUtil.is(quickTag.getId()));
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickSkill.class)).data(quickTag)
				.message("QuickSkill Saved");
	}

	// QuickLocations
	@RequestMapping(value = "/api/tmpl/quick/location", method = { RequestMethod.GET })
	public ApiResponse<QuickLocation, Object> listQuickLocations() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLocation.class));
	}

	@RequestMapping(value = "/api/tmpl/quick/location", method = { RequestMethod.DELETE })
	public ApiResponse<QuickLocation, Object> deleteQuickLocations(@RequestParam String id) {
		QuickLocation qr = mongoTemplate.removeAndAudit(id, QuickLocation.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLocation.class)).data(qr)
				.message("QuickLocation deleted");
	}

	@RequestMapping(value = "/api/tmpl/quick/location", method = { RequestMethod.POST })
	public ApiResponse<QuickLocation, Object> createQuickLocation(@RequestBody QuickLocation req) {
		QuickLocation quickTag = quickStore.createGalleryItem(req, new QuickLocation());
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLocation.class)).data(quickTag)
				.message("QuickLocations Saved");
	}

	// KnowledgeBase
	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.GET })
	public ApiResponse<KnowBase, Object> listKnowBase() {
		return ApiResponse.buildResults(mongoTemplate.findAll(KnowBase.class));
	}

	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.DELETE })
	public ApiResponse<KnowBase, Object> deleteKnowBase(@RequestParam String id) {
		KnowBase qr = mongoTemplate.removeAndAudit(id, KnowBase.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(KnowBase.class)).data(qr).message("KnowBase deleted");
	}

	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.POST })
	public ApiResponse<KnowBase, Object> createKnowBase(@RequestBody KnowBase req) {
		KnowBase newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new KnowBase());
		newVersion.setCategory(req.getCategory());
		newVersion.setTitle(req.getTitle());
		newVersion.setContent(req.getContent());
		mongoTemplate.saveAndAudit(newVersion, ArgUtil.is(newVersion.getId()));
		return ApiResponse.buildResults(mongoTemplate.findAll(KnowBase.class)).data(newVersion)
				.message("KnowBase Saved");
	}
}
