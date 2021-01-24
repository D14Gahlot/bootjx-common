package com.boot.jx.postman.notify;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.BranchesBHR;
import com.boot.jx.dict.BranchesKWT;
import com.boot.jx.dict.Nations;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.client.PushNotifyClient;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.scope.tnt.Tenants.Tenant;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * The Class NotifyController.
 */
@RestController
@Api(value = "Push Notifiation APIs")
public class NotifyController {

	/** The logger. */
	private Logger logger = Logger.getLogger(NotifyController.class);

	/** The fb push client. */
	@Autowired
	PushNotifyClient pushNotifyClient;

	/**
	 * List of tenants.
	 *
	 * @return the list
	 * @throws PostManException     the post man exception
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException   the execution exception
	 */
	@RequestMapping(value = PostManUrls.LIST_TENANT, method = RequestMethod.POST)
	public List<Tenant> listOfTenants() throws PostManException, InterruptedException, ExecutionException {
		return Tenants.values();
	}

	/**
	 * List of nations.
	 *
	 * @return the list
	 * @throws PostManException     the post man exception
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException   the execution exception
	 */
	@RequestMapping(value = PostManUrls.LIST_NATIONS, method = RequestMethod.POST)
	public List<Nations> listOfNations() throws PostManException, InterruptedException, ExecutionException {
		return Arrays.asList(Nations.values());
	}

	/**
	 * Notify all.
	 *
	 * @param tenant  the tenant
	 * @param message the message
	 * @param title   the title
	 * @return the push message
	 * @throws PostManException the post man exception
	 */
	@RequestMapping(value = "/postman/notify/all", method = RequestMethod.POST)
	public ApiResponse<PushMessage, Object> notifyAll(
			@ApiParam(required = true, allowableValues = "KWT,BHR", value = "Select Tenant") @RequestParam String tenant,
			@RequestParam String message, @RequestParam String title) throws PostManException {
		PushMessage msg = new PushMessage();
		msg.setMessage(message);
		msg.setSubject(title);
		msg.addToTenant(tenant);
		return pushNotifyClient.sendDirect(msg);
	}

	/**
	 * Notify national.
	 *
	 * @param tenant      the tenant
	 * @param nationality the nationality
	 * @param message     the message
	 * @param title       the title
	 * @return the push message
	 * @throws PostManException the post man exception
	 */
	@RequestMapping(value = "/postman/notify/nationality", method = RequestMethod.POST)
	public ApiResponse<PushMessage, Object> notifyNational(
			@ApiParam(required = true, allowableValues = "KWT,BHR", value = "Select Tenant") @RequestParam String tenant,
			@RequestParam Nations nationality, @RequestParam String message, @RequestParam String title)
			throws PostManException {
		PushMessage msg = new PushMessage();
		msg.setMessage(message);
		msg.setSubject(title);

		if (nationality == Nations.ALL) {
			msg.addToTenant(tenant);
		} else {
			msg.addToCountry(tenant, nationality.getCode());
		}
		return pushNotifyClient.sendDirect(msg);
	}

}
