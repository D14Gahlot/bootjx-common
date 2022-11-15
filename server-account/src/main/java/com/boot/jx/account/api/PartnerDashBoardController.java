package com.boot.jx.account.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.dto.AccountDashBoardResponseDto;
import com.boot.jx.account.dto.ContactTypeSummaryDto;
import com.boot.jx.account.dto.MonthDtlsDto;
import com.boot.jx.account.dto.WabaSummaryDocDto;
import com.boot.jx.account.manager.AccountDashBoardManager;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.http.CommonHttpRequest;

@Controller
@RequestMapping("/partnerdashboard")
public class PartnerDashBoardController {

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private AccountSessionBean adminSessionBean;

	@Autowired
	private AccountStore accountStore;

	@Autowired
	AccountDashBoardManager dashBMgr;

	@ResponseBody
	@RequestMapping(value = { "/pub/domain" }, method = { RequestMethod.GET })
	public ApiResponse<DomainDoc, Object> getDomain() {
		List<DomainDoc> domainDocLst = dashBMgr.getAllDomainAccount();
		return ApiResponse.buildResults(domainDocLst);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/admin/fetch-month" }, method = { RequestMethod.GET })
	public ApiResponse<MonthDtlsDto, Object> getMonthLst() {
		List<MonthDtlsDto> listofMonth = dashBMgr.fetchUniqueMonth();
		return ApiResponse.buildResults(listofMonth);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/admin/monthwise-summary-count" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getMonthWiseSummary(long timestamp) {
		ContactTypeSummaryDto summary = dashBMgr.getMonthWiseCount(timestamp);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/account/dashboard" }, method = { RequestMethod.GET })
	public ApiResponse<AccountDashBoardResponseDto, Object> getAccountDashboardDetils() {
		AccountDashBoardResponseDto response = null;// dashBMgr.getAccountDashBoardDetails();
		return ApiResponse.buildResult(response);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/monthwise-summary-save" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getMonthWiseSaving(long timestamp) {
		ContactTypeSummaryDto summary = dashBMgr.summaryV1(timestamp);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/monthwise-summary/waba" }, method = { RequestMethod.GET })
	public ApiResponse<WabaSummaryDocDto, Object> getMonthWiseWabaSummary(long timestamp) {
		List<WabaSummaryDocDto> summary = dashBMgr.wabaSummary(timestamp);
		return ApiResponse.buildResults(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/hourwise-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getHourWiseSummary(@RequestParam(required = false) long timestamp,
			long hr) {
		ContactTypeSummaryDto summary = dashBMgr.hourWisesummary(timestamp, hr);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/daywise-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getDayWiseSummary(@RequestParam(required = false) long dateRange1,
			@RequestParam(required = false) long dateRange2, int days) {
		ContactTypeSummaryDto summary = dashBMgr.dayChannelWiseWisesummary(dateRange1, dateRange2, days);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/hourwise-msg-status-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getHourWiseMsgStatusSummary(
			@RequestParam(required = false) long timestamp, long hr) {
		ContactTypeSummaryDto summary = dashBMgr.getHourWiseMsgStatusSummary(timestamp, hr);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/datewise-msg-status-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getDayWiseMsgStatusSummary(long dateRange1, long dateRange2,
			int days) {
		ContactTypeSummaryDto summary = dashBMgr.getDayWiseMsgStatusSummary(dateRange1, dateRange2, days);
		return ApiResponse.buildResult(summary);
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/non-whatsup-msg-summary" }, method = { RequestMethod.GET })
	public ApiResponse<ContactTypeSummaryDto, Object> getNonWhatsUpSummary(long dateRange1, long dateRange2) {
		ContactTypeSummaryDto summary = dashBMgr.getNonWhatsUpSummary(dateRange1, dateRange2);
		return ApiResponse.buildResult(summary);
	}

}
