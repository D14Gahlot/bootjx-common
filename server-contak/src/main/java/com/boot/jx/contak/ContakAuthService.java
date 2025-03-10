package com.boot.jx.contak;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.common.config.PMCommonConfigImpl;
import com.boot.jx.contak.doc.ContakMembershipDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.rest.AppRequestInterfaces.AppAuthUser;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;

@Component
public class ContakAuthService implements LogoutHandler, AuditDetailProvider {

	/*
	 * Below APIs are
	 * 
	 * APIs for currently logged in user only
	 */

	@Autowired
	private ContakSessionBean sessionBean;

	@Autowired
	private ContakAuthProvider adminAuthProvider;

	@Autowired
	private RestService restService;

	@Autowired
	private PMCommonConfigImpl appCommonConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Value("${mry.duperadmin.email}")
	private String duperEmail;

	@Value("${mry.superadmin.pass}")
	private String duperPass;

	public void updateSession() {
	}

	public ContakUserDoc loadUserByUsername(String username) throws UsernameNotFoundException {
		ContakUserDoc user = null;
		if (duperEmail.equalsIgnoreCase(username)) {
			user = new ContakUserDoc();
			user.setName("DuperAdmin");
			user.setEmail(duperEmail);
			user.meta().setPassword(duperPass);
			user.setActive(true);
		} else {
			user = commonMongoTemplate.collection(ContakUserDoc.class).where("email", username).find().asFirst();
		}
		return user;
	}

	public ContakMembershipDoc addMembership(ContakUserDoc user, CompanyDoc company, String membershipType) {
		ContakMembershipDoc m = commonMongoTemplate
				.collection(ContakMembershipDoc.class).where(QueryCriteria.where("user.$id")
						.is(new ObjectId(user.getId())).and("company.$id").is(new ObjectId(company.getCompanyId())))
				.find().asFirst();
		if (ArgUtil.not(m)) {
			m = new ContakMembershipDoc();
			m.setCompany(company);
			m.setUser(user);
			m.setCompanyId(company.getCompanyId());
			m.setUserId(user.getId());
		}
		m.setActive(ArgUtil.is(membershipType));
		m.setMembershipType(membershipType);
		commonMongoTemplate.save(m);
		return m;
	}

	/**
	 * Refreshes login status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogin(ContakUserDoc account) {
		sessionBean.domainUser(account);
		sessionBean.addRole(CollectionUtil.asArray(account.getRole()));
		sessionBean.addRole(PMConstants.USER_ROLE.BUSINESS_USER);
		if (ArgUtil.areEqual(appCommonConfig.getDuperEmail(), account.getEmail())) {
			sessionBean.addRole(PMConstants.USER_ROLE.DUPER_USER);
		}
		List<ContakMembershipDoc> m = commonMongoTemplate.collection(ContakMembershipDoc.class)
				.where("user.id", account.getId()).find().asList();
		sessionBean.memberships(m);
		this.updateSession();
	}

	/**
	 * Refreshes logout status for currently logged in agent
	 * 
	 * @param username
	 */
	public void updateLogout(String username) {
		sessionBean.domainUser(null);
		SecurityContextHolder.getContext().setAuthentication(null);
		this.updateSession();
	}

	public void login(ContakUserDoc account, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(account.getEmail(),
				account.getMeta().getPassword());
		token.setDetails(new WebAuthenticationDetails(request));
		Authentication authentication = adminAuthProvider.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		updateLogin(account);
	}

	public ContakUserDoc authenticate(String username, String password, HttpServletRequest request) {
		ContakUserDoc user = loadUserByUsername(username);
		if (user == null || !CryptoUtil.getEncoder().message(password).sha2().is(user.meta().getPassword())) {
			return null;
		}
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
		token.setDetails(new WebAuthenticationDetails(request));
		Authentication authentication = adminAuthProvider.authenticate(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		updateLogin(user);
		return user;
	}

	@Autowired
	private PostManClient postManClient;

	@Autowired
	private PMEnvironment pmEnvironment;

	public void sendResetMail(ContakUserDoc accountDoc, String emailTemplate, String verifyCode) {
		postManClient
				.send(new MessageBox().push(new Email().to(accountDoc.getEmail()).template(emailTemplate)
						.put("logo", pmEnvironment.config().prefsEntry("mry.prop.logo.bg-x-icon").asString())
						.put("website", pmEnvironment.config().prefsEntry("mry.prop.service.website").asString())
						.put("service", pmEnvironment.config().prefsEntry("mry.prop.service.name").asString())
						.put("servicedomain", pmEnvironment.commonConfig().getServiceServerByRequest())
						.put("link", String.format("https://%s.%s/contak/panel/auth/verify-link?code=%s&account=%s",
								AppContextUtil.getTenant(), pmEnvironment.commonConfig().getServiceServerByRequest(),
								verifyCode, accountDoc.getEmail()))
						.put("contactName", accountDoc.getName())));

	}

	public static Authentication getAuthentication() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth) && auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
			return auth;
		}
		return null;
	}

	public static boolean isAuthenticated() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth) && auth instanceof UsernamePasswordAuthenticationToken && auth.isAuthenticated()) {
			return true;
		}
		return false;
	}

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		if (ArgUtil.is(authentication)) {
			updateLogout(ArgUtil.parseAsString(authentication.getPrincipal()));
		}
		commonHttpRequest.instance(request, response, appConfig).setCookie("CONTAKSESSIONID", "CONTAKSESSIONID", 0);
	}

	@Override
	public AppAuthUser getAuthUser() {
		return this.sessionBean;
	}

	@Override
	public String getAuditUser() {
		if (RequestContextHolder.getRequestAttributes() != null) {
			if (ArgUtil.is(getAuthUser())) {
				return getAuthUser().getAuthUser();
			}
		}
		return PMConstants.DEFAULT.NO_USER;
	}

}
