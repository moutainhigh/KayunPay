package com.dutiantech.config.routes;

import com.dutiantech.controller.export.DateInterfaceController;
import com.dutiantech.controller.portal.ActionController;
import com.dutiantech.controller.portal.ActivityController;
import com.dutiantech.controller.portal.BankV2Controller;
import com.dutiantech.controller.portal.ExportFileController;
import com.dutiantech.controller.portal.FanLiTouController;
import com.dutiantech.controller.portal.FanLiTouControllerV2;
import com.dutiantech.controller.portal.FundsController;
import com.dutiantech.controller.portal.LeaveNoteController;
import com.dutiantech.controller.portal.LoanCenterController;
import com.dutiantech.controller.portal.PlatformController;
import com.dutiantech.controller.portal.PortalController;
import com.dutiantech.controller.portal.QuestionController;
import com.dutiantech.controller.portal.TicketController;
import com.dutiantech.controller.portal.UserCenterController;
import com.jfinal.config.Routes;

public class PortalRoutes extends Routes{

	public void config() {
		add("portal_userCenter",UserCenterController.class);
		add("portal_loanCenter",LoanCenterController.class);
		add("portal_portal" ,PortalController.class ) ;
		add("portal_funds" ,FundsController.class ) ;
		add("portal_action",ActionController.class);
		add("bankv2",BankV2Controller.class);
		add("activity",ActivityController.class);
		add("portal_ticket", TicketController.class);
		add("wdzj_data", DateInterfaceController.class);
		add("fanlitou", FanLiTouController.class);
		add("platform", PlatformController.class);
		add("fanlitouV2", FanLiTouControllerV2.class);
		add("exportFile",ExportFileController.class);
		add("questionController", QuestionController.class);
		add("leaveNoteController", LeaveNoteController.class);
	}

}
