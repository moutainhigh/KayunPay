package com.dutiantech.controller.app;

import com.dutiantech.controller.BaseController;
import com.jfinal.core.ActionKey;

public class ActionController extends BaseController {
	
	/**
	 * ------------------------ HTML   路径 映射 --------------------------
	 * 							(路径page开头)
	 */
	@ActionKey("/")
	public void index() {
		renderText("success");
	}
	
	@ActionKey("/app_act20171201")
	public void appAct20171201() {
		forward("/app/act20171128.html");
	}
	
	@ActionKey("/app_actDetails20171201")
	public void appActDetails20171201(){
		forward("/app/actDetails20171128.html");
	}
	
	@ActionKey("/app_act20180101")
	public void appAct20180101(){
		forward("/app/act20180101.html");
	}
	
	@ActionKey("/app_actDetails20180101")
	public void appActDetails20180101(){
		forward("/app/actDetails20180101.html");
	}
	
	@ActionKey("/app_actInvite")
	public void app_actInvite(){
		forward("/app/actInvite.html");
	}
	
	@ActionKey("/app_actInviteDetails")
	public void app_actInviteDetails(){
		forward("/app/actInviteDetails.html");
	}
	
	@ActionKey("/app_assessment")
	public void app_assessment(){
		forward("/app/assessment.html");
	}
	
	@ActionKey("/app_assessment01")
	public void app_assessment01(){
		forward("/app/assessment01.html");
	}
	
	@ActionKey("/app_grade")
	public void app_grade(){
		forward("/app/grade.html");
	}
	
	@ActionKey("/app_gradeDetails_01")
	public void app_gradeDetails_01(){
		forward("/app/gradeDetails_01.html");
	}
	
	@ActionKey("/app_gradeDetails_02")
	public void app_gradeDetails_02(){
		forward("/app/gradeDetails_02.html");
	}
	
	@ActionKey("/app_gradeDetails_03")
	public void app_gradeDetails_03(){
		forward("/app/gradeDetails_03.html");
	}
	
	@ActionKey("/app_gradeDetails_04")
	public void app_gradeDetails_04(){
		forward("/app/gradeDetails_04.html");
	}
	
	@ActionKey("/app_gradeDetails_05")
	public void app_gradeDetails_05(){
		forward("/app/gradeDetails_05.html");
	}
	
	@ActionKey("/app_gradeDetails_06")
	public void app_gradeDetails_06(){
		forward("/app/gradeDetails_06.html");
	}
	
	@ActionKey("/app_gradeDetails_07")
	public void app_gradeDetails_07(){
		forward("/app/gradeDetails_07.html");
	}
	
	@ActionKey("/app_gradeDetails_08")
	public void app_gradeDetails_08(){
		forward("/app/gradeDetails_08.html");
	}
	
	@ActionKey("/app_MessageMenu")
	public void app_MessageMenu(){
		forward("/app/MessageMenu.html");
	}
	
	@ActionKey("/app_Message01")
	public void app_Message01(){
		forward("/app/Message01.html");
	}
	
	@ActionKey("/app_Message03")
	public void app_Message03(){
		forward("/app/Message03.html");
	}
	
	@ActionKey("/app_Message04")
	public void app_Message04(){
		forward("/app/Message04.html");
	}
	
	@ActionKey("/app_Message07")
	public void app_Message07(){
		forward("/app/Message07.html");
	}
	
	@ActionKey("/app_Message08")
	public void app_Message08(){
		forward("/app/Message08.html");
	}
	
	@ActionKey("/app_Message08_1")
	public void app_Message08_1(){
		forward("/app/Message08_1.html");
	}
	@ActionKey("/app_Message08_2")
	public void app_Message08_2(){
		forward("/app/Message08_2.html");
	}
	
	@ActionKey("/app_Message08_3")
	public void app_Message08_3(){
		forward("/app/Message08_3.html");
	}
	@ActionKey("/app_Message08_4")
	public void app_Message08_4(){
		forward("/app/Message08_4.html");
	}
	@ActionKey("/app_Message08_5")
	public void app_Message08_5(){
		forward("/app/Message08_5.html");
	}
	
	@ActionKey("/app_Message09")
	public void app_Message09(){
		forward("/app/Message09.html");
	}
	
	@ActionKey("/app_HelpCenter")
	public void app_HelpCenter(){
		forward("/app/HelpCenter.html");
	}
	
	@ActionKey("/app_BankDepository")
	public void app_BankDepository(){
		forward("/app/BankDepository.html");
	}
	
	@ActionKey("/app_BankPayment")
	public void app_BankPayment(){
		forward("/app/BankPayment.html");
	}
	
}