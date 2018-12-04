package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNxjd;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNxjdService;
import com.dutiantech.util.DateUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.tx.Tx;

public class LoanNxjdController extends BaseController{

	private LoanNxjdService loanNxjdService = getService(LoanNxjdService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	@ActionKey("/scanNxjdBiao")
	@Before({Tx.class, PkMsgInterceptor.class})
	public void scanNxjdBiao() {
		Message msg = updateNxjdTask() ;
		renderJson(msg);
	}
	
	@SuppressWarnings("unchecked")
	private Message updateNxjdTask() {
		String key = getPara("key", "");
		if (!key.equals("3.14159265358")) {
			return error("01", "密匙错误", false);
		}
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("status", LoanNxjd.STATUS.A.key());
		List<LoanNxjd> lstLoanNxjd = loanNxjdService.findByCondition(condition);
		for (LoanNxjd loanNxjd : lstLoanNxjd) {
			String loanCode = loanNxjd.getStr("loanCode");
			LoanInfo loanInfo = loanInfoService.findById(loanCode);
			if (LoanInfo.LoanState.N.val().equals(loanInfo.getStr("loanState"))) {
				int sumBorrowTime = loanNxjdService.sumBorrowTimeByLoanCode(loanCode);
				String expireDate = DateUtil.addDay(loanInfo.getStr("effectDate"), sumBorrowTime);	// 计算到期时间
				int compareDateResult = DateUtil.compareDateByStr("yyyyMMdd", expireDate, DateUtil.getNowDate());
				if ("9c418ca39cba4b3cac2ea04c4529254e".equals(loanCode)) {
					System.out.println("sleep");
				}
				if (compareDateResult == -1) {
					loanNxjd.set("status", LoanNxjd.STATUS.B.key());
					loanNxjdService.saveOrUpdate(loanNxjd);
				}
			}
		}
		return succ("自动扫描更新智投盈关联标信息任务完成", true);
	}
	
}
