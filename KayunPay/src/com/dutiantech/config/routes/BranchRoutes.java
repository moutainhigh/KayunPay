package com.dutiantech.config.routes;

import com.dutiantech.controller.LoanRepaymentController;
import com.dutiantech.controller.branch.JXCallbackController;
import com.dutiantech.controller.branch.YiStageNotifyController;
import com.jfinal.config.Routes;

public class BranchRoutes extends Routes{

	@Override
	public void config() {
		add("jxCallbackController", JXCallbackController.class);	// 江西银行存管异步回调接口
		add("yiStageNotifyController", YiStageNotifyController.class);	// 易分期异步回调接口
		
		// 自用接口
		add("loanRepaymentController", LoanRepaymentController.class);
	}

}
