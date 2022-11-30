package com.boot.jx.contak.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.ContakAuthService;
import com.boot.jx.contak.ContakSessionBean;
import com.boot.jx.contak.doc.ContakMembershipDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Contak Panel", description = "API's for Panel", hidden = true)
@Controller
@RequestMapping("/panel")
public class PanelController {
	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ContakAuthService authService;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private ContakSessionBean sessionBean;

	@ApiOperation(value = "Page", hidden = true)
	@RequestMapping(path = { "", "/", "/**" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String defaultPage(Model model) {
		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_NAME", sessionBean.domainUser().getName());
			model.addAttribute("APP_USER_ROLE", JsonUtil.toJson(sessionBean.getRole()));
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_NAME", "User");
			model.addAttribute("APP_USER_ROLE", "['GUEST']");
		}
		return "app-contak";
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/login" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> login(Model model, @RequestParam String username, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) {
		ContakUserDoc user = authService.authenticate(username, password, request);
		return ApiResponse.build().meta(MapModel.createInstance().put("username", user.getName()).toMap());
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/companys" }, method = { RequestMethod.GET })
	public ApiResponse<ContakMembershipDoc, Object> organization(Model model) {
		List<ContakMembershipDoc> m = commonMongoTemplate.collection(ContakMembershipDoc.class)
				.where(Criteria.where("user.id").is(sessionBean.domainUser().getId()).and("active").is(true)).find()
				.asList();
		sessionBean.memberships(m);
		return ApiResponse.buildResults(m);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/memberships" }, method = { RequestMethod.POST })
	public ApiResponse<ContakMembershipDoc, Object> addmember(Model model, @RequestParam String companyId,
			@RequestParam String username, @RequestParam(required = false) String membershipType) {

		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}

		ContakUserDoc user = authService.loadUserByUsername(username);

		if (ArgUtil.not(user)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("username").codeKey("InvalidUsername").description("User not found"));
		}

		CompanyDoc company = commonMongoTemplate.collection(CompanyDoc.class).where(QueryCriteria.whereId(companyId))
				.find().asFirst();;
		if (ArgUtil.not(company)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("InvalidCompany").description("Company not found"));
		}

		ContakMembershipDoc m = commonMongoTemplate.collection(ContakMembershipDoc.class)
				.where(QueryCriteria.where("userId").is(user.getId()).and("companyId").is(companyId)).find().asFirst();
		if (ArgUtil.not(m)) {
			m = new ContakMembershipDoc();
			m.setCompany(company);
			m.setUser(user);
		}
		m.setActive(ArgUtil.is(membershipType));
		m.setMembershipType(membershipType);
		commonMongoTemplate.save(m);
		return ApiResponse.buildResult(m);
	}
}
