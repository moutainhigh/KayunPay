package com.dutiantech.config.routes;

import com.dutiantech.controller.app.ActionController;
import com.dutiantech.controller.app.ActivityController;
import com.dutiantech.controller.app.AppLoanCenterController;
import com.dutiantech.controller.app.AppLoanInfoController;
import com.dutiantech.controller.app.AppProtalController;
import com.dutiantech.controller.app.AppTicketController;
import com.dutiantech.controller.app.AppTransferController;
import com.dutiantech.controller.app.BankV2Controller;
import com.dutiantech.controller.app.AppFundsController;
import com.dutiantech.controller.app.UserController;
import com.jfinal.config.Routes;

public class AppRoutes extends Routes{

	@Override
	public void config() {
		add("appUserController", UserController.class);
		add("appFundsController", AppFundsController.class);
		add("appProtalController", AppProtalController.class);
		add("appLoanInfoController", AppLoanInfoController.class);
		add("appBankv2Controller", BankV2Controller.class);
		add("appTransferController", AppTransferController.class);
		add("appTicketController", AppTicketController.class);
		add("appLoanCenterController", AppLoanCenterController.class);
		add("appActionController", ActionController.class);
		add("appActivityController", ActivityController.class);
	}

}
