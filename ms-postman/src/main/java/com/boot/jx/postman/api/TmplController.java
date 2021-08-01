package com.boot.jx.postman.api;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.Language;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.model.ITemplates.TemplateDefaultEnum;
import com.boot.jx.postman.model.PostManFile;
import com.boot.jx.postman.service.FileService;
import com.boot.jx.postman.service.PostManServiceImpl;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

/**
 * The Class PostManController.
 */
@RestController
public class TmplController {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TmplController.class);

    /** The request. */
    @Autowired
    private HttpServletRequest request;

    /** The post man config. */
    @Autowired
    private PostManConfig postManConfig;

    @Autowired
    private FileService fileService;

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    private String getLang(CommonFile file) {
	if (ArgUtil.isEmpty(file) || ArgUtil.isEmpty(file.getLang())) {
	    String langString = request.getParameter(PostManServiceImpl.PARAM_LANG);// localeResolver.resolveLocale(request).toString();
	    Language lang = ArgUtil.parseAsEnumT(langString, postManConfig.getTenantLang(), Language.class);
	    file.lang(lang);
	}
	return file.getLang();
    }

    /**
     * Process template.
     *
     * @param template the template
     * @param data     the data
     * @param fileName the file name
     * @param fileType the file type
     * @return the file
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = TmplClient.PATH.TMPL_FILE_PROCESS, method = RequestMethod.GET)
    public ApiResponse<PostManFile, Object> processTemplate(@RequestParam TemplateDefaultEnum template,
	    @RequestParam(required = false) String data, @RequestParam(required = false) String fileName,
	    @RequestParam(required = false) FileFormat fileType,
	    @RequestParam(required = false) ContactType contactType) {

	PostManFile file = new PostManFile();
	getLang(file);

	file.setITemplate(template);
	file.setFileFormat(fileType);
	file.setModel(JsonUtil.fromJson(data, Map.class));
	return ApiResponse.buildResult(fileService.create(file, contactType));

    }

    /**
     * Process template file.
     *
     * @param file the file
     * @return the file
     */
    @RequestMapping(value = { TmplClient.PATH.TMPL_FILE_PROCESS }, method = RequestMethod.POST)
    public ApiResponse<PostManFile, Object> processTemplateFile(@RequestBody PostManFile file,
	    @RequestParam(required = false) ContactType contactType) {
	getLang(file);
	return ApiResponse.buildResult(fileService.create(file, contactType));
    }

}
