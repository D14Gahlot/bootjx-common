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
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.dto.AccountDashBoardResponseDto;
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
    private AccountAdminService sessionService;

    @Autowired
    private AccountSessionBean adminSessionBean;

    @Autowired
    private AccountStore accountStore;

    @Autowired
    AccountDashBoardManager dashBMgr;
  
    
    @ResponseBody
    @RequestMapping(value = { "/pub/domain" }, method = { RequestMethod.GET })
    public ApiResponse<DomainDoc, Object> getDomain() {
	List<DomainDoc> domainDocLst =dashBMgr.getAllDomainAccount();
	return ApiResponse.buildResults(domainDocLst);
    }
    
    
    @ResponseBody
    @RequestMapping(value = { "/pub/account/dashboard" }, method = { RequestMethod.GET })
    public ApiResponse<AccountDashBoardResponseDto, Object> getAccountDashboardDetils() {
	AccountDashBoardResponseDto response =null;//dashBMgr.getAccountDashBoardDetails();
	return ApiResponse.buildResult(response);
    }
    

}
