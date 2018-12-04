package com.dutiantech.controller.portal;

import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class ActionController extends BaseController {
	
	/**
	 * ------------------------ HTML   路径 映射 --------------------------
	 * 							(路径page开头)
	 */
	@ActionKey("/cfcaAgreement")
	public void cfcaAgreement(){
		forward("/portal/cfcaAgreement.html",true);
	}
	
	@ActionKey("/payFailed")
	public void payFailed(){
		forward("/pay/payFaild.html",false);
	}

	@ActionKey("/paySuccess")
	public void paySuccess(){
		forward("/pay/paySuccess.html",true);
	}
	@ActionKey("/aboutus")
	public void aboutus(){
		forward("/portal/mobile/aboutUs.html",true);
	}
	@ActionKey("/ispaysuccforapp")
	public void ispaysuccforapp(){
		forward("/portal/mobile/ispaysuccforapp.html",true);
	}
	@ActionKey("/payFaild")
	public void payFaild() {
		forward("/pay/payFaild.html", true);
	}
	@ActionKey("/withdrawFaild")
	public void withdrawFaild() {
		forward("/pay/withdrawFaild.html", true);
	}
	@ActionKey("/withdrawSuccess")
	public void withdrawSuccess() {
		forward("/pay/withdrawSuccess.html", true);
	}
	@ActionKey("/guide")
	public void guide(){
		forward("/portal/mobile/guide.html");
	}
	@ActionKey("/secure")
	public void secure(){
		forward("/portal/mobile/secure.html",true);
	}
	@ActionKey("/rule")
	public void rule(){
		forward("/portal/mobile/rule.html",true);
	}
	@ActionKey("/rule1")
	public void rule1(){
		forward("/portal/mobile/rule1.html",true);
	}
	@ActionKey("/rule2")
	public void rule2(){
		forward("/portal/mobile/rule2.html",true);
	}
	@ActionKey("/rule3")
	public void rule3(){
		forward("/portal/mobile/rule3.html",true);
	}
	@ActionKey("/rule4")
	public void rule4(){
		forward("/portal/mobile/rule4.html",true);
	}
	@ActionKey("/rule5")
	public void rule5(){
		forward("/portal/mobile/rule5.html",true);
	}
	@ActionKey("/N_detail")
	public void N_detail(){
		forward("/portal/mobile/N_detail.html",true);
	}
	@ActionKey("/N_index")
	public void N_index(){
		forward("/portal/mobile/N_index.html",true);
	}
	@ActionKey("/error")
	public void error(){
		forward("/portal/404.html");
	}
	@ActionKey("/calculator")
	public void calculator(){
		forward("/portal/calculator.html");
	}
	@ActionKey("/A00")
	public void A00(){
		forward("/portal/A00.html");
	}
	@ActionKey("/A01")
	public void A01(){
		forward("/portal/A01.html");
	}
	@ActionKey("/A02")
	public void A02(){
		forward("/portal/A02.html");
	}
	@ActionKey("/A03")
	public void A03(){
		forward("/portal/A03.html");
	}
	@ActionKey("/A04")
	public void A04(){
		forward("/portal/A04.html");
	}
	@ActionKey("/B01")
	public void B01(){
		forward("/portal/B01.html");
	}
	@ActionKey("/B02")
	public void B02(){
		forward("/portal/B02.html");
	}
	@ActionKey("/B03")
	public void B03(){
		forward("/portal/B03.html");
	}
	@ActionKey("/B04")
	public void B04(){
		forward("/portal/B04.html");
	}
	@ActionKey("/B05")
	public void B05(){
		forward("/portal/B05.html");
	}
	@ActionKey("/C01")
	public void C01(){
		forward("/portal/C01.html");
	}
	@ActionKey("/modifyHfMobile")
	public void modifyHfMobile(){
		forward("/portal/modifyHfMobile.html");
	}
	@ActionKey("/appModifyHfMobileNotice")
	public void appModifyHfMobile(){
		forward("/portal/mobile/appModifyHfMobileNotice.html");
	}
	/*
	@ActionKey("/C02")
	public void C02(){
		forward("/portal/C02.html");
	}
	*/
	@ActionKey("/C03")
	public void C03(){
		forward("/portal/C03.html");
	}
	@ActionKey("/C04")
	public void C04(){
		forward("/portal/C04.html");
	}
	@ActionKey("/D01")
	public void D01(){
		forward("/portal/D01.html");
	}
	@ActionKey("/D02")
	public void D02(){
		forward("/portal/D02.html");
	}
	@ActionKey("/D03")
	public void D03(){
		forward("/portal/D03.html");
	}

	@ActionKey("/findPwd")
	public void findPwd(){
		forward("/portal/findPwd.html");
	}

	@ActionKey("/")
	public void index(){
		forward("/portal/index.html");
	}

	@ActionKey("/login")
	public void login(){
		forward("/portal/login.html");
	}
	
	@ActionKey("/mailLogin")
	public void mailLogin(){
		forward("/portal/mailLogin.html");
	}

	@ActionKey("/mailFail")
	public void mailFail(){
		forward("/portal/mailFail.html");
	}

	@ActionKey("/mailSuccess")
	public void mailSuccess(){
		forward("/portal/mailSuccess.html");
	}

	@ActionKey("/register")
	public void register(){
		forward("/portal/register.html");
	}
	@ActionKey("/register_t")
	public void register_t(){
		forward("/portal/register_t.html");
	}
	@ActionKey("/register_suc")
	public void register_suc(){
		forward("/portal/register_suc.html");
	}
	@ActionKey("/X00")
	public void X00(){
		forward("/portal/X00.html");
	}
	@ActionKey("/X01")
	public void X01(){
		forward("/portal/X01.html");
	}
	@ActionKey("/X02")
	public void X02(){
		forward("/portal/X02.html");
	}
	@ActionKey("/X03")
	public void X03(){
		forward("/portal/X03.html");
	}
	@ActionKey("/X04")
	public void X04(){
		forward("/portal/X04.html");
	}
	@ActionKey("/X05")
	public void X05(){
		forward("/portal/X05.html");
	}
	@ActionKey("/X06")
	public void X06(){
		forward("/portal/X06.html");
	}
	@ActionKey("/X07")
	public void X07(){
		forward("/portal/X07.html");
	}
	@ActionKey("/X08")
	public void X08(){
		forward("/portal/X08.html");
	}
	@ActionKey("/X09")
	public void X09(){
		forward("/portal/X09.html");
	}
	@ActionKey("/Y01")
	public void Y01(){
		forward("/portal/Y01.html");
	}
	@ActionKey("/Y02")
	public void Y02(){
		forward("/portal/Y02.html");
	}
	@ActionKey("/Y03")
	public void Y03(){
		forward("/portal/Y03.html");
	}
	@ActionKey("/Y04")
	public void Y04(){
		forward("/portal/Y04.html");
	}
	@ActionKey("/Y05_01")
	public void Y05_01(){
		forward("/portal/Y05_01.html");
	}
	@ActionKey("/Y05_02")
	public void Y05_2(){
		forward("/portal/Y05_02.html");
	}
	@ActionKey("/Y05_03")
	public void Y05_03(){
		forward("/portal/Y05_03.html");
	}
	@ActionKey("/Y05_04")
	public void Y05_04(){
		forward("/portal/Y05_04.html");
	}
	@ActionKey("/Y05_05")
	public void Y05_05(){
		forward("/portal/Y05_05.html");
	}
	@ActionKey("/Y06_06_1")
	public void Y06_06_1(){
		forward("/portal/Y06_06_1.html");
	}
	@ActionKey("/Y06_01")
	public void Y06_01(){
		forward("/portal/Y06_01.html");
	}
	@ActionKey("/Y06_02")
	public void Y06_02(){
		forward("/portal/Y06_02.html");
	}
	@ActionKey("/Y06_03")
	public void Y06_03(){
		forward("/portal/Y06_03.html");
	}
	@ActionKey("/Y06_04")
	public void Y06_04(){
		forward("/portal/Y06_04.html");
	}
	@ActionKey("/Y06_05")
	public void Y06_05(){
		forward("/portal/Y06_05.html");
	}
	@ActionKey("/Y06_06")
	public void Y06_06(){
		forward("/portal/Y06_06.html");
	}
	@ActionKey("/Y06_07")
	public void Y06_07(){
		forward("/portal/Y06_07.html");
	}
	@ActionKey("/Y06_08")
	public void Y06_8(){
		forward("/portal/Y06_08.html");
	}
	@ActionKey("/Y06_08_1")
	public void Y06_08_1(){
		forward("/portal/Y06_08_1.html");
	}
	@ActionKey("/Y06_09")
	public void Y06_09(){
		forward("/portal/Y06_09.html");
	}
/*	@ActionKey("/Y06_10")
	public void Y06_10(){
		forward("/portal/Y06_10.html");
	}
	@ActionKey("/Y06_11")
	public void Y06_11(){
		forward("/portal/Y06_11.html");
	}*/
	
	@ActionKey("/Z02_1")
	public void Z02_1(){
		forward("/portal/Z02_1.html");
	}
	
	@ActionKey("/Z02_2")
	public void Z02_2(){
//		String aCode = UIDUtil.generate() ;
//		//加入验证码识别码
//		setCookie("cac_z02_v1", aCode , 60) ;
//		CACHED.put( aCode , "12|12" , true , 60 );	//设置buid和sid
		forward("/portal/Z02_2.html");
	}
	
	@ActionKey("/Z02")
	public void Z02(){
		forward("/portal/Z02.html");
	}
	@ActionKey("/Z03_1")
	public void Z03_1(){
		forward("/portal/Z03_1.html");
	}
	@ActionKey("/Z03")
	public void Z03(){
		forward("/portal/Z03.html");
	}
	@ActionKey("/Z04")
	public void Z04(){
		forward("/portal/Z04.html");
	}
	@ActionKey("/Z05")
	public void Z05(){
		forward("/portal/Z05.html");
	}
	@ActionKey("/K01")
	public void K01(){
		forward("/portal/K01.html");
	}
	@ActionKey("/K02")
	public void K02(){
		forward("/portal/K02.html");
	}
	@ActionKey("/K03")
	public void K03(){
		forward("/portal/K03.html");
	}
	@ActionKey("/K04")
	public void K04(){
		forward("/portal/K04.html");
	}
	@ActionKey("/K05")
	public void K05(){
		forward("/portal/K05.html");
	}
	@ActionKey("/K06")
	public void K06(){
		forward("/portal/K06.html");
	}
	@ActionKey("/K07")
	public void K07(){
		forward("/portal/K07.html");
	}
	@ActionKey("/agreement")
	public void agreement(){
		forward("/portal/agreement.html");
	}
	@ActionKey("/loanAgreement")
	public void loanAgreement(){
		forward("/portal/loanAgreement.html");
	}
	@ActionKey("/loanUserAgreement")
	public void loanUserAgreement(){
		forward("/portal/loanUserAgreement.html");
	}
	@ActionKey("/share")
	public void share(){
		String userCode = getPara("u","");
		if( StringUtil.isBlank( userCode ) == false )
			setCookie("fc", userCode, 3*24*60*60 );
		forward("/portal/register.html");
	}
	
	@ActionKey("/share4mobile")
	public void share4mobile(){
		String userCode = getPara("u","");
		if( StringUtil.isBlank( userCode ) == false )
			setCookie("fc", userCode, 3*24*60*60 );
//		forward("http://m.yrhx.com/m/#reg3");
		redirect("http://m.yrhx.com/m/#reg3");
	}
	
	@ActionKey("/loginToGasture")
	public void loginToGasture(){
		forward("/portal/wx/gasture.html");
	}
	
	@ActionKey("/loginToMobile")
	public void loginToMobile(){
		forward("/portal/wx/mobile.html");
	}

	@ActionKey("/K08")
	public void K08() {
		forward("/portal/K08.html");
	}
	
	@ActionKey("/product_details")
	public void product_details() {
		forward("/portal/product_details.html");
	}
	
	@ActionKey("/act0801")
	public void act0801() {
		forward("/portal/act0801.html");
	}
	
	@ActionKey("/act0901")
	public void act0901() {
		forward("/portal/act0901.html");
	}
	
	@ActionKey("/act201710")
	public void act201710() {
		forward("/portal/act201710.html");
	}
	
	@ActionKey("/CDKEY")
	public void redeemCode() {
		forward("/portal/redeemCode.html");
	}
	
	@ActionKey("/act201711")
	public void act201711() {
		forward("/portal/act201711.html");
	}
	
	@ActionKey("/act20171118")
	public void act20171118() {
		forward("/portal/act20171118.html");
	}

	@ActionKey("/showMessage")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class})
	public void showMessage(){
		String userCode = getUserCode();
		if (!"e960414c3c7f4a7598be0a8302d95de2".equals(userCode)) {
			forward("/portal/index.html");
		}
		forward("/pages/showMessage.html");
	}
	
	@ActionKey("/act20171201")
	public void act20171201() {
		forward("/portal/act20171127.html");
	}
	@ActionKey("/A05")
	public void A05(){
		forward("/portal/A05.html");
	}
	
	@ActionKey("/Y07")
	public void Y07(){
		forward("/portal/Y07.html");
	}
	
	@ActionKey("/act20180101")
	public void act20180101(){
		forward("/portal/act20180101.html");
	}
	
	@ActionKey("/act_novice2018")
	public void act_novice2018(){
		forward("/portal/act_novice2018.html");
	}
	
	@ActionKey("/appRegister")
	public void appRegister(){
		forward("/portal/appRegister.html");
	}
	
	@ActionKey("/appDownload")
	public void appDownload(){
		forward("/portal/appDownload.html");
	}
	
	@ActionKey("/assessment")
	public void assessment(){
		forward("/portal/assessment.html");
	}
	
	@ActionKey("/assessment01")
	public void assessment01(){
		forward("/portal/assessment01.html");
	}
	
	@ActionKey("/Message01")
	public void Message01(){
		forward("/portal/Message01.html");
	}
	
	@ActionKey("/Message03")
	public void Message03(){
		forward("/portal/Message03.html");
	}
	
	@ActionKey("/Message04")
	public void Message04(){
		forward("/portal/Message04.html");
	}
	
	@ActionKey("/Message06")
	public void Message06(){
		forward("/portal/Message06.html");
	}
	
	@ActionKey("/Message07")
	public void Message07(){
		forward("/portal/Message07.html");
	}
	
	@ActionKey("/Message08")
	public void Message08(){
		forward("/portal/Message08.html");
	}
	
	@ActionKey("/Message07_01")
	public void Message07_01(){
		forward("/portal/Message07_01.html");
	}
	
	@ActionKey("/Message07_02")
	public void Message07_02(){
		forward("/portal/Message07_02.html");
	}
	
	@ActionKey("/Message07_03")
	public void Message07_03(){
		forward("/portal/Message07_03.html");
	}
	
	@ActionKey("/Message07_04")
	public void Message07_04(){
		forward("/portal/Message07_04.html");
	}
	
	@ActionKey("/Message07_05")
	public void Message07_05(){
		forward("/portal/Message07_05.html");
	}
	
	@ActionKey("/Message09")
	public void Message09(){
		forward("/portal/Message09.html");
	}
	
	@ActionKey("/Message10")
	public void Message10(){
		forward("/portal/Message10.html");
	}
	
	@ActionKey("/grade")
	public void grade(){
		forward("/portal/grade.html");
	}
	
	@ActionKey("/aiServer")
	public void aiServer(){
		forward("/portal/aiServer.html");
	}
	
	@ActionKey("/loanProtocolDetal")
	public void loanProtocolDetal(){
		forward("/portal/loanProtocolDetal.html");
	}
	
	@ActionKey("/ShareHeader")
	public void ShareHeader(){
		forward("/portal/ShareHeader.html");
	}
	
	@ActionKey("/LendersEducation")
	public void LendersEducation(){
		forward("/portal/LendersEducation.html");
	}
	
	@ActionKey("/depositoryUser")
	public void depositoryUser(){
		forward("/portal/depositoryUser.html");
	}
	
	@ActionKey("/register_success")
	public void register_success(){
		forward("/portal/register_success.html");
	}
	
	@ActionKey("/depositoryUserUsed")
	public void depositoryUserUsed(){
		forward("/portal/depositoryUserUsed.html");
	}
	
	@ActionKey("/BankDepository")
	public void BankDepository(){
		forward("/portal/BankDepository.html");
	}
	
	@ActionKey("/act20180518")
	public void act20180518(){
		forward("/portal/act20180518.html");
	}
	
	@ActionKey("/ios")
	public void iosDownload() {
		forward("/ios/index.html");
	}
	
	@ActionKey("/J01")
	public void J01(){
		forward("/portal/J01.html");
	}
	@ActionKey("/xy1")
	public void xy1(){
		forward("/portal/xy1.html");
	}
	@ActionKey("/xy2")
	public void xy2(){
		forward("/portal/xy2.html");
	}
	@ActionKey("/xy3")
	public void xy3(){
		forward("/portal/xy3.html");
	}
	@ActionKey("/Z06")
	public void z06(){
		forward("/portal/Z06.html");
	}
}