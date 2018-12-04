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
		add("app_userController", UserController.class);
		add("app_fundsController", AppFundsController.class);
		add("app_protalController", AppProtalController.class);
		add("app_loanInfoController", AppLoanInfoController.class);
		add("app_bankv2Controller", BankV2Controller.class);
		add("app_transferController", AppTransferController.class);
		add("app_ticketController", AppTicketController.class);
		add("app_loanCenterController", AppLoanCenterController.class);
		add("app_actionController", ActionController.class);
		add("app_activityController", ActivityController.class);
	}

}
