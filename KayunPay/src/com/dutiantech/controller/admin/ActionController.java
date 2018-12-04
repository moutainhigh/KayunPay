package com.dutiantech.controller.admin;

import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.util.CommonUtil;
import com.jfinal.core.ActionKey;

public class ActionController extends BaseController{
	
	/**
	 * --------------------- HTML 路径 映射 BEGIN--------------------------
	 * 							(路径page开头)
	 */

	@ActionKey("/login")
	public void login(){
//		String aCode = UIDUtil.generate() ;
//		//加入验证码识别码
//		setCookie("cac_login_v1", aCode , 60) ;
//		CACHED.put( aCode , "11|11" , true , 60 );	//设置buid和sid
		forward("/login.html");
	}
	
	@ActionKey("/first")
	public void first(){
		forward("/pages/default.html");
	}
	
	@ActionKey("/main")
	public void main(){
		forward("/main.html");
	}
	
	@ActionKey("/pageUserMgrForm")
	public void pageUserMgrForm(){
		forward("/pages/admin-user-manager-form.html");
	}
	
	@ActionKey("/pageTransferMgrForm")
	public void pageTransferMgrForm(){
		forward("/pages/admin-transfer-info-form.html");
	}
	
	@ActionKey("/pageRoleForm")
	public void pageRoleForm(){
		forward("/pages/admin-role-form.html");
	}
	
	@ActionKey("/pageFundsForm")
	public void pageFundsForm(){
		forward("/pages/funds-form.html");;
	}
	
	@ActionKey("/pageFundsTraceForm")
	public void pageFundsTraceForm(){
		forward("/pages/funds-trace-form.html");;
	}
	
	@ActionKey("/pageFundsTraceList")
	public void pageFundsTraceList(){
		forward("/pages/funds-trace-list.html");
	}
	
	@ActionKey("/pageLoanBasicForm")
	public void pageLoanBasicForm(){
		forward("/pages/loan-basic-form.html");
	}
	
	@ActionKey("/pageLoanForm")
	public void pageLoanForm(){
		forward("/pages/loan-form.html");
	}
	
	@ActionKey("/mLoanPubList")
	public void mLoanPubList(){
		forward("/pages/loan-pub-list.html");
	}
	
	@ActionKey("/pageLoanList")
	public void pageLoanList(){
		forward("/pages/loan-list.html");
	}
	
	@ActionKey("/pageLoanTraceList")
	public void pageLoanTraceList(){
		forward("/pages/loan-trace-list.html");
	}
	
	@ActionKey("/pageLoanNoticeForm")
	public void pageLoanNoticeForm(){
		forward("/pages/loan-notice-form.html");
	}
	
	@ActionKey("/pageLoanNoticeList")
	public void pageLoanNoticeList(){
		forward("/pages/loan-notice-list.html");
	}
	
	@ActionKey("/pageLoanTransferForm")
	public void pageLoanTransferForm(){
		forward("/pages/loan-transfer-form.html");;
	}
	
	@ActionKey("/pageLoanOverdueList")
	public void pageLoanOverdueList(){
		forward("/pages/loan-overdue-list.html");;
	}
	
	@ActionKey("/pageLoanOverdueForm")
	public void pageLoanOverdueForm(){
		forward("/pages/loan-overdue-form.html");;
	}

	@ActionKey("/pageUserList")
	public void pageUserList(){
		forward("/pages/user-list.html");
	}
	
	@ActionKey("/pageUserForm")
	public void pageUserForm(){
		forward("/pages/user-form.html");
	}
	
	@ActionKey("/pageUserAuthedForm")
	public void pageUserAuthedForm(){
		forward("/pages/user-authed-form.html");
	}
	
	@ActionKey("/pageUserDeposit")
	public void pageUserDeposit(){
		forward("/pages/user-deposit-account.html");
	}
	
	@ActionKey("/pageUserLoanTraceList")
	public void pageUserLoanTraceList(){
		forward("/pages/user-loan-trace-list.html");
	}
	
	@ActionKey("/pageCreateLoanNotice")
	public void pageCreateLoanNotice(){
		forward("/pages/loan-notice-create-form.html");
	}
	@ActionKey("/pageCreatePlatformUser")
	public void pageCreatePlatformUser(){
		forward("/pages/user-create.html");
	}
	
	@ActionKey("/pageLoanApplyForm")
	public void pageLoanFormBak(){
		forward("/pages/loan-apply-form.html");
	}
	
	@ActionKey("/pageLoanApplyList")
	public void pageLoanApplyList(){
		forward("/pages/loan-apply-list.html");
	}
	
	@ActionKey("/pageLoanPubList")
	public void pageLoanPubList(){
		forward("/pages/loan-pub-list.html");
	}
	
	@ActionKey("/bizLog")
	public void bizLog(){
		forward("/pages/biz-log-list.html");
	}
	
	@ActionKey("/bizLogForm")
	public void bizLogForm(){
		forward("/pages/biz-log-form.html");
	}
	
	@ActionKey("/bizAutoLoanList")
	public void bizAutoLoanList(){
		forward("/pages/biz-autoLoan-list.html");
	}
	
	@ActionKey("/pageContentNewsForm")
	public void pageContentNewsForm(){
		forward("/pages/content-news-form.html");
	}
	
	@ActionKey("/pageShopProductForm")
	public void pageShopProductForm(){
		forward("/pages/shop-product-form.html");
	}
	
	@ActionKey("/pageShopExchangedForm")
	public void pageShopExchangedForm(){
		forward("/pages/shop-exchanged-form.html");
	}
	
	@ActionKey("/pageTJJLForm")
	public void pageTJJLForm(){
		forward("/pages/user-tj-form.html");
	}
	
	@ActionKey("/pageRechargeTraceList")
	public void pageRechargeTraceList(){
		forward("/pages/funds-cz-list.html");
	}
	
	@ActionKey("/pageWithdrawTraceList")
	public void pageWithdrawTraceList(){
		forward("/pages/funds-tx-list.html");
	}
	
	@ActionKey("/pageJiangQuanForm")
	public void pageTicketForm(){
		forward("/pages/jiangquan-form.html");
	}
	
	@ActionKey("/pageUserScoreDetail")
	public void pageUserScoreDetail(){
		forward("/pages/user-score-detail.html");
	}
	
	@ActionKey("/contract")
	public void contract(){
		forward("/portal/contract.html");
	}
	
	@ActionKey("/contractV2")
	public void contractV2() {
		forward("/portal/contractV2.html");
	}
	
	@ActionKey("/contractV3")
	public void contractV3(){
		forward("/portal/contractV3.html");
	}
	
	@ActionKey("/pageContractsFrom")
	public void pageContractsFrom(){
		forward("/pages/contracts-form.html");
	}
	
	/**
	 * --------------------- HTML 路径 映射      END--------------------------
	 * 							(路径page开头)
	 */
	
	/**
	 * --------------------- 系统菜单 路径 映射   BEGIN--------------------------
	 * 							(路径m开头)
	 */
	
	@AuthNum(value=10,pval=10,desc="系统管理")
	public void mSysMgr(){
	}
	
	@ActionKey("/mOpManager")
	@AuthNum(value=11,pval=10,desc="操作员管理")
	public void mOpManager(){
		forward("/pages/admin-user-manager-list.html");
	}
	
	@ActionKey("/mMenuManager")
	@AuthNum(value=12,pval=10,desc="菜单管理")
	public void mMenuManager(){
		forward("/pages/admin-menu-manager.html");
	}
	
	@ActionKey("/mRoleManager")
	@AuthNum(value=13,pval=10,desc="角色管理")
	public void mRoleManager(){
		forward("/pages/admin-role-list.html");
	}
	
	@ActionKey("/mTransferInfo")
	@AuthNum(value=75,pval=10,desc="债权人管理")
	public void mTransferInfo(){
		forward("/pages/admin-transfer-info-list.html");
	}
	
	@ActionKey("/mPlatFormSettings")
	@AuthNum(value=200,pval=10,desc="平台配置")
	public void mPlatFormSettings(){
		forward("/pages/admin-plaform-setting-list.html");
	}
	
	@ActionKey("/mPlatFormSettingsForm") 
	@AuthNum(value=201,pval=10,desc="平台配置表单")
	public void mPlatFormSettingsForm(){
		forward("/pages/admin-platform-setting-form.html");
	}
	
	
	@AuthNum(value=14,pval=14,desc="发标申请")
	public void mLoanApplyMgr(){
		
	}

	@ActionKey("/mLoanApplyList")
	@AuthNum(value=15,pval=14,desc="申请列表")
	public void mLoanApplyNewList(){
		forward("/pages/loan-apply-list.html");
	}
	
	@ActionKey("/mLoanApplyUserList")
	@AuthNum(value=16,pval=14,desc="借款人列表")
	public void mLoanApplyUserList(){
		forward("/pages/loan-apply-user-list.html");
	}
	
	@ActionKey("/mLoanApplyForm")
	@AuthNum(value=17,pval=14,desc="申请贷款")
	public void mLoanApplyForm(){
		redirect("/pageLoanApplyForm?opType=new");
	}
	
	@ActionKey("/mLoanApplyXSList")
	@AuthNum(value=18,pval=14,desc="信审")
	public void mLoanApplyXSList(){
		forward("/pages/loan-apply-xs-list.html");
	}
	
	@ActionKey("/mLoanApplyAuditList")
	@AuthNum(value=19,pval=14,desc="风控审核")
	public void mLoanApplyAuditList(){
		forward("/pages/loan-apply-audit-list.html");
	}
	
	@ActionKey("/mLoanToWithdraw")
	@AuthNum(value = 20, pval = 14, desc = "借款人提现")
	public void loanToWithdraw(){
		forward("/pages/loan-to-withdraw.html");
	}
	
	@AuthNum(value=26,pval=26,desc="贷款管理")
	public void mLoanMgr(){
		
	}
	
	@ActionKey("/mHKGL")
	@AuthNum(value=27,pval=26,desc="还款管理")
	public void mHKGL(){
		forward("/pages/loan-hk-list.html");
	}
	
	@ActionKey("/mLoanJRHK")
	@AuthNum(value=28,pval=26,desc="今日还款")
	public void mLoanJRHK(){
		forward("/pages/loan-jrhk-list.html");
	}
	
	@ActionKey("/mLoanMade")
	@AuthNum(value=29,pval=26,desc="制作新标")
	public void mLoanMade(){
		forward("/pages/loan-made-list.html");
	}
	
	@ActionKey("/mPubLoanManager")
	@AuthNum(value=30,pval=26,desc="待发布")
	public void mPubLoanManager(){
		forward("/pages/loan-pub-list.html");
	}
	
	@ActionKey("/mLoanZB")
	@AuthNum(value=31,pval=26,desc="招标中")
	public void mLoanZB(){
		forward("/pages/loan-zbz-list.html");
	}
	
	@ActionKey("/mLoanAuditMgr")
	@AuthNum(value=32,pval=26,desc="已满标")
	public void mLoanAuditMgr(){
		forward("/pages/loan-audit-list.html");
	}
	
	@ActionKey("/mOverLoan")
	@AuthNum(value=33,pval=26,desc="已流标")
	public void mLoanOverdueManager(){
		forward("/pages/loan-ylb-list.html");
	}
	
	@ActionKey("/mLoanList")
	@AuthNum(value=34,pval=26,desc="贷款列表")
	public void mLoanList(){
		forward("/pages/loan-list.html");
	}
	
	@ActionKey("/mCJFBGG")
	@AuthNum(value=35,pval=26,desc="创建发标公告")
	public void mLoanHK(){
		forward("/pages/loan-notice-create-form.html");
	}
	
	@ActionKey("/mLoanNoticeManager")
	@AuthNum(value=36,pval=26,desc="审核发标公告")
	public void mLoanNoticeManager(){
		forward("/pages/loan-notice-list.html");
	}
	
	@ActionKey("/repaymentDay")
	@AuthNum(value=37,pval=26,desc="当日还款")
	public void repaymentDay(){
		forward("/pages/loan-repaymentByDay.html");
	}
	
	@ActionKey("/repaymentDayList")
	@AuthNum(value=38,pval=26,desc="当日还款列表")
	public void repaymentDayList(){
		forward("/pages/loan-repaymentDayList.html");
	}
	
	@ActionKey("/mOverdueLoantransfer")
	@AuthNum(value=38,pval=26,desc="逾期债转列表")
	public void overdueLoantransferList(){
		forward("/pages/overdueLoan-transfer-list.html");
	}
	
	@AuthNum(value=39,pval=39,desc="财务管理")
	public void mFundManager(){
	}
	
	@ActionKey("/mLiCaiRenAccount")
	@AuthNum(value=40,pval=39,desc="理财人账户")
	public void mLiCaiRenAccount(){
		forward("/pages/funds-mananger1-list.html");
	}
	
	@ActionKey("/mJieKuanRenAccount")
	@AuthNum(value=41,pval=39,desc="借款人账户")
	public void mJieKuanRenAccount(){
		forward("/pages/funds-mananger2-list.html");
	}

	@ActionKey("/mWithdrawalsTraceList")
	@AuthNum(value=42,pval=39,desc="提现流水")
	public void mWithdrawalsTraceList(){
		forward("/pages/funds-tx-list.html");
	}
	
	@ActionKey("/mRechargeTraceList")
	@AuthNum(value=43,pval=39,desc="充值流水")
	public void mRechargeTraceList(){
		forward("/pages/funds-cz-list.html");
	}
	
	@ActionKey("/mfundsTrace")
	@AuthNum(value=44,pval=39,desc="资金流水")
	public void fundTrace(){
		forward("/pages/funds-trace-list.html");
	}
	
	@ActionKey("/mWithdrawalsAudit")
	@AuthNum(value=45,pval=39,desc="提现管理")
	public void mWithdrawalsAudit(){
		forward("/pages/funds-tixian-sh-list.html");
	}
	
	@ActionKey("/voucherPayTrace")
	@AuthNum(value = 46, pval = 39, desc = "红包发放")
	public void voucherPayTrace(){
		forward("/pages/funds-jx-voucherPay.html");
	}
	
	@ActionKey("/mUpdateBank")
	@AuthNum(value=46,pval=39,desc="修改银行卡号")
	public void mUpdateBank(){
		if ("lianlian".equals(CommonUtil.PAY_INTERFACE)) {
			forward("/pages/update-bank-form.html");	
		} else if ("fuiou".equals(CommonUtil.PAY_INTERFACE)) {
			forward("/pages/update-bank-fuiou.html");
		}
	}
	
	@ActionKey("/mLoanListWithdraw")
	@AuthNum(value=46,pval=39,desc="放款提现列表")
	public void mLoanListWithdraw(){
		forward("/pages/loan-list-withdraw.html");
	}
	
	@ActionKey("/mUnbindBank4LianLian")
	@AuthNum(value=47,pval=39,desc="解绑理财卡(LL)")
	public void mUnbindBank4LianLian(){
		forward("/pages/update-unbindbank4lianlian-form.html");
	}
	
	@ActionKey("/mABCDEFG")
	@AuthNum(value=48,pval=39,desc="商银信商户提现")
	public void mABCDEFG(){
		forward("/pages/syx-adv.html");
	}
	
	@ActionKey("/mFkList")
	@AuthNum(value=49,pval=39,desc="放款列表")
	public void mFkList(){
		forward("/pages/loan-fk-list.html");
	}
	
	@AuthNum(value=49,pval=49,desc="用户管理")
	public void mPlatformUserManager(){
	}
	
	@ActionKey("/mPlatformUserList")
	@AuthNum(value=50,pval=49,desc="用户中心")
	public void mPlatformUserList(){
		forward("/pages/user-list.html");
	}
	
	@ActionKey("/mPlatformUserAuthed")
	@AuthNum(value=51,pval=49,desc="认证中心")
	public void mPlatformUserAuthed(){
		forward("/pages/user-authed-list.html");
	}
	
	@ActionKey("/mPlatformUserScore")
	@AuthNum(value=52,pval=49,desc="积分管理")
	public void mPlatformUserScore(){
		forward("/pages/user-score-list.html");
	}
	
	@ActionKey("/mPlatFormUserDetailList")
	@AuthNum(value=54,pval=49,desc="积分明细")
	public void mPlatFormUserDetailList(){
		forward("/pages/user-score-detail.html");
	}
	
	@ActionKey("/mTJJL")
	@AuthNum(value=53,pval=49,desc="推荐奖励")
	public void mTJYJ(){
		forward("/pages/user-tj-list.html");
	}
	
	@ActionKey("/mUpdateMobile")
	@AuthNum(value=55,pval=49,desc="修改手机号")
	public void mUpdateMobile(){
		forward("/pages/update-mobile-form.html");
	}
	
	@AuthNum(value=58,pval=58,desc="内容管理")
	public void mWebContentManager(){
	}
	
	@ActionKey("/mContentNews")
	@AuthNum(value=59,pval=58,desc="文章信息")
	public void mDocManager(){
		forward("/pages/content-news-list.html");
	}
	@ActionKey("/mPushMessage")
	@AuthNum(value=60,pval=58,desc="APP消息推送")
	public void mPushMessage(){
		forward("/pages/app-message-form.html");
	}
	
	@ActionKey("/mSendMobileMsg")
	@AuthNum(value=61,pval=58,desc="发送手机短信")
	public void mSendMobileMsg(){
		forward("/pages/send-message-form.html");
	}
	
	@ActionKey("/mQuestion")
	@AuthNum(value=62,pval=58,desc="问题列表")
	public void mQuestion(){
		forward("/pages/content-question-list.html");
	}
	
	@ActionKey("/mLeavenote")
	@AuthNum(value=63,pval=58,desc="留言列表")
	public void mLeavenote(){
		forward("/pages/content-leaveNote-list.html");
	}
	
	@ActionKey("/mCreatQuestion")
	public void mCreatQuestion(){
		forward("/pages/content-question-form.html");
	}
	
	@AuthNum(value=65,pval=65,desc="积分商城")
	public void mScoreShop(){
	}
	
	@ActionKey("/mProductManager")
	@AuthNum(value=66,pval=65,desc="商品管理")
	public void mProductManager(){
		forward("/pages/shop-product-list.html");
	}
	
	@ActionKey("/mExchangedList")
	@AuthNum(value=67,pval=65,desc="兑换明细")
	public void mExchangedList(){
		forward("/pages/shop-exchanged-list.html");
	}
	
	@AuthNum(value=70,pval=70,desc="奖券")
	public void mJiangQuan(){
	}
	
	@ActionKey("/mCreateJiangQuan")
	@AuthNum(value=71,pval=70,desc="新增奖券")
	public void mCreateJiangQuan(){
		forward("/pages/jiangquan-form.html");
	}
	
	@ActionKey("/mJiangQuanList")
	@AuthNum(value=72,pval=70,desc="奖券列表")
	public void mJiangQuanList(){
		forward("/pages/jiangquan-list.html");
	}
	
	@ActionKey("/mSubTicket")
	@AuthNum(value=73,pval=70,desc="奖券列表")
	public void mSubTicket(){
		forward("/pages/sub-ticket-form.html");
	}
	
	@ActionKey("/mCreateRewardRateTicket")
	@AuthNum(value=74,pval=70,desc="新增加息券")
	public void mCreateJiaXi(){
		forward("/pages/jiaxiquan-form.html");
	}

	@ActionKey("/mBatchPoints")
	@AuthNum(value=74,pval=49,desc="批量积分修改")
	public void mBatchPoints() {
		forward("/pages/batch-points-form.html");
	}
	
	@ActionKey("/mBatchTickets")
	@AuthNum(value = 75, pval = 49, desc = "批量发放奖券")
	public void mBatchTickets() {
		forward("/pages/batch-tickets-form.html");
	}
	
	@ActionKey("/mBatchRateTickets")
	@AuthNum(value = 75, pval = 49, desc = "批量发放加息券")
	public void mBatchRateTickets() {
		forward("/pages/batch-rateTickets-form.html");
	}
	
	@ActionKey("/mUserJqList")
	@AuthNum(value = 75, pval = 49, desc = "个人投资加权金额查询")
	public void mUserJqList() {
		forward("/pages/funds-jq-list.html");
	}
	
	@ActionKey("/mContractTemplate")
	@AuthNum(value = 76,pval = 10,desc="合同模板管理")
	public void mContractTemplate(){
		forward("/pages/admin-contractTemplate-list.html");
	}
	@ActionKey("/pageTemplateForm")
	public void pageTemplateForm(){
		forward("/pages/admin-contractTemplate-form.html");
	}
	@AuthNum(value = 80, pval = 80, desc = "抽奖管理")
	public void mPrize() {
	}
	
	@ActionKey("/mPrizeRecord")
	@AuthNum(value = 81, pval = 80, desc = "抽奖记录")
	public void mPrizeRecord() {
		forward("/pages/prize-record-list.html");
	}
	
	@AuthNum(value=90, pval=90, desc="合同")
	public void mContract(){
	}
	@ActionKey("/mContMrg")
	@AuthNum(value=91, pval=90, desc="合同管理")
	public void mContMrg(){
		forward("/pages/contracts.html");
	}
	
	@ActionKey("/openAccount")
	@AuthNum(value = 75, pval = 49, desc = "接力贷用户开户")
	public void mFuiou_addAcount() {
		forward("/pages/admin-fuiou-addAcount.html");
	}
	
	@ActionKey("/testJxUpload")
	public void testJxUpload() {
		forward("/pages/admin-test-upLoad.html");
	}
	
	@AuthNum(value = 100,pval = 100,desc="即信后台查询")
	public void jxManage(){
		
	}
	
	@ActionKey("/adminJx")
	@AuthNum(value = 101, pval = 100, desc = "电子账户余额查询")
	public void adminJx(){
		forward("/pages/admin-jx-balanceQuery.html");
	}
	
	@ActionKey("/jxAccountDetailsQuery2")
	@AuthNum(value = 102, pval = 100, desc = "近两日电子账户资金交易明细")
	public void jxAccountDetailsQuery2(){
		forward("/pages/admin-jx-accountDetailsQuery2.html");
	}
	
	@ActionKey("/jxCardBindDetailsQuery")
	@AuthNum(value = 103, pval = 100, desc = "绑卡关系查询")
	public void jxCardBindDetailsQuery(){
		forward("/pages/admin-jx-cardBindDetailsQuery.html");
	}
	
	@ActionKey("/jxCreditDetailsQuery")
	@AuthNum(value = 104, pval = 100, desc = "投资人债权明细查询")
	public void jxCreditDetailsQuery(){
		forward("/pages/admin-jx-creditDetailsQuery.html");
	}
	
	@ActionKey("/jxBidApplyQuery")
	@AuthNum(value = 105, pval = 100, desc = "投资人投标申请查询")
	public void jxBidApplyQuery(){
		forward("/pages/admin-jx-bidApplyQuery.html");
	}
	
	@ActionKey("/jxFreezeAmtQuery")
	@AuthNum(value = 106, pval = 100, desc = "电子账户各项冻结金额查询")
	public void jxFreezeAmtQuery(){
		forward("/pages/admin-jx-freezeAmtQuery.html");
	}
	
	@ActionKey("/jxAccountIdQuery")
	@AuthNum(value = 107, pval = 100, desc = "按证件号查询电子账号")
	public void jxAccountIdQuery(){
		forward("/pages/admin-jx-accountIdQuery.html");
	}
	
	@ActionKey("/jxDebtDetailsQuery")
	@AuthNum(value = 108, pval = 100, desc = "借款人标的信息查询")
	public void jxDebtDetailsQuery(){
		forward("/pages/admin-jx-debtDetailsQuery.html");
	}
	
	@ActionKey("/jxMobileMaintainace")
	@AuthNum(value = 109, pval = 100, desc = "电子账户手机号查询")
	public void jxMobileMaintainace(){
		forward("/pages/admin-jx-mobileMaintainace.html");
	}
	
	@ActionKey("/jxAccountQueryByMobile")
	@AuthNum(value = 110, pval = 100, desc = "手机号查询电子账号")
	public void jxAccountQueryByMobile(){
		forward("/pages/admin-jx-accountQueryByMobile.html");
	}
	
	@ActionKey("/jxTransactionStatusQuery")
	@AuthNum(value = 111, pval = 100, desc = "查询交易状态")
	public void jxTransactionStatusQuery(){
		forward("/pages/admin-jx-transactionStatusQuery.html");
	}
	
	@ActionKey("/jxBatchQuery")
	@AuthNum(value = 112, pval = 100, desc = "查询批次状态")
	public void jxBatchQuery(){
		forward("/pages/admin-jx-batchQuery.html");
	}
	
	@ActionKey("/jxBatchDetailsQuery")
	@AuthNum(value = 113, pval = 100, desc = "查询批次交易明细")
	public void jxBatchDetailsQuery(){
		forward("/pages/admin-jx-batchDetailsQuery.html");
	}
	
	@ActionKey("/jxCreditInvestQuery")
	@AuthNum(value = 114, pval = 100, desc = "投资人购买债权查询")
	public void jxCreditInvestQuery(){
		forward("/pages/admin-jx-creditInvestQuery.html");
	}
	
	@ActionKey("/jxCreditAuthQuery")
	@AuthNum(value = 115, pval = 100, desc = "投资人签约状态查询")
	public void jxCreditAuthQuery(){
		forward("/pages/admin-jx-creditAuthQuery.html");
	}
	
	@ActionKey("/jxCorprationQuery")
	@AuthNum(value = 116, pval = 100, desc = "企业账户查询")
	public void jxCorprationQuery(){
		forward("/pages/admin-jx-corprationQuery.html");
	}
	
	@ActionKey("/jxFrzDetailsQuery")
	@AuthNum(value = 117, pval = 100, desc = "账户资金冻结明细查询")
	public void jxFrzDetailsQuery(){
		forward("/pages/admin-jx-frzDetailsQuery.html");
	}
	
	@ActionKey("/jxPasswordSetQuery")
	@AuthNum(value = 118, pval = 100, desc = "电子账户密码是否设置查询")
	public void jxPasswordSetQuery(){
		forward("/pages/admin-jx-passwordSetQuery.html");
	}
	
	@ActionKey("/jxBalanceFreezeQuery")
	@AuthNum(value = 119, pval = 100, desc = "单笔还款申请冻结查询")
	public void jxPalanceFreezeQuery(){
		forward("/pages/admin-jx-balanceFreezeQuery.html");
	}
	
	@ActionKey("/jxBatchVoucherDetailsQuery")
	@AuthNum(value = 120, pval = 100, desc = "查询批次发红包交易明细")
	public void jxBatchVoucherDetailsQuery(){
		forward("/pages/admin-jx-batchVoucherDetailsQuery.html");
	}
	
	@ActionKey("/jxFundTransQuery")
	@AuthNum(value = 121, pval = 100, desc = "单笔资金类业务交易查询")
	public void jxFundTransQuery(){
		forward("/pages/admin-jx-fundTransQuery.html");
	}
	
	@ActionKey("/jxCreditInvesDetailsQuery")
	@AuthNum(value = 123, pval = 100, desc = "债权转让明细查询")
	public void jxCreditInvesDetailsQuery(){
		forward("/pages/admin-jx-creditInvesDetailsQuery.html");
	}
	
	@ActionKey("/jxTermsAuthQuery")
	@AuthNum(value = 123, pval = 100, desc = "客户授权功能查询")
	public void jxTermsAuthQuery(){
		forward("/pages/admin-jx-termsAuthQuery.html");
	}
	
	@ActionKey("/jxFundTransQueryForList")
	@AuthNum(value = 124, pval = 100, desc = "单笔资金类查询（列表）")
	public void jxFundTransQueryForList(){
		forward("/pages/admin-jx-fundTransQuery2.html");
	}
	@ActionKey("/adminJxTrace")
	@AuthNum(value = 125, pval = 100, desc = "即信本地数据流水查询")
	public void aadminJx(){
		forward("/pages/admin-jx-trace.html");
	}
	@ActionKey("/jxQueryAleveFromDownFile")
	@AuthNum(value = 125, pval = 100, desc = "aleve记录")
	public void jxQueryAleveFromDownFile(){
		forward("/pages/admin-jx-aleveQuery.html");
	}
	
	@ActionKey("/jxTransfer")
	@AuthNum(value = 127, pval = 39, desc = "代扣代付")
	public void jxTransfer(){
		forward("/pages/funds-jx-jxTransfer.html");
	}
	
	

	/**
	 * --------------------- 系统菜单 路径 映射       END--------------------------
	 * 							(路径m开头)
	 */
	
}