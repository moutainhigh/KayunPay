package com.dutiantech.config.db;

import com.dutiantech.model.AuthLog;
import com.dutiantech.model.AutoLoan;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.AutoMap;
import com.dutiantech.model.BankCode;
import com.dutiantech.model.BankOreaCode;
import com.dutiantech.model.Banks;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog;
import com.dutiantech.model.CFCAInfo;
import com.dutiantech.model.ChangeBankTrace;
import com.dutiantech.model.ContractTemplate;
import com.dutiantech.model.Contracts;
import com.dutiantech.model.FanLiTouUserInfo;
import com.dutiantech.model.Files;
import com.dutiantech.model.FuiouTrace;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.HistoryRecy;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LeaveNote;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNotice;
import com.dutiantech.model.LoanNxjd;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.Market;
import com.dutiantech.model.MarketUser;
import com.dutiantech.model.Menu;
import com.dutiantech.model.Notice;
import com.dutiantech.model.OPUser;
import com.dutiantech.model.Permit;
import com.dutiantech.model.PrizeRecords;
import com.dutiantech.model.Prizes;
import com.dutiantech.model.Question;
import com.dutiantech.model.QuestionnaireRecords;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.RecommendReward;
import com.dutiantech.model.Redeem;
import com.dutiantech.model.ReturnedAmount;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.Share;
import com.dutiantech.model.ShareAward;
import com.dutiantech.model.ShortPassword;
import com.dutiantech.model.SignTrace;
import com.dutiantech.model.SliderPic;
import com.dutiantech.model.SysConfig;
import com.dutiantech.model.SysFunds;
import com.dutiantech.model.TempReport;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.TransferWay;
import com.dutiantech.model.User;
import com.dutiantech.model.UserCount;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.model.ViewSysCount;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.model.YistageTrace;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

public class PublicDB {
	public static void addMapping( ActiveRecordPlugin arp ){

		arp.addMapping("t_user", "userCode", User.class );
		arp.addMapping("t_user_info", "userCode", UserInfo.class);
		arp.addMapping("t_menu", "menu_id", Menu.class);
		arp.addMapping("t_permit", "pemit_index", Permit.class);
		arp.addMapping("t_funds", "userCode", Funds.class);
		arp.addMapping("t_loan_info", "loanCode", LoanInfo.class);
		arp.addMapping("t_funds_trace", "traceCode", FundsTrace.class);
		arp.addMapping("t_loan_trace", "traceCode",LoanTrace.class);
		arp.addMapping("t_op_user", "userCode",OPUser.class);
		arp.addMapping("t_files", "remoteCode",Files.class);
		arp.addMapping("t_auto_loan", "userCode",AutoLoan.class);
		arp.addMapping("t_withdraw_trace","withdrawCode",WithdrawTrace.class);
		arp.addMapping("t_loan_overdue","overdueCode", LoanOverdue.class);
		arp.addMapping("t_loan_notice","noticeCode", LoanNotice.class);
		arp.addMapping("t_loan_transfer", "transCode", LoanTransfer.class);
		arp.addMapping("t_bizlog", "bizNo", BizLog.class);
		arp.addMapping("t_loan_apply", "loanNo",LoanApply.class);
		arp.addMapping("t_sys_funds", "id",SysFunds.class);
		arp.addMapping("t_banks", "bankCode",Banks.class);
		arp.addMapping("t_bank_Code", "bankCodes",BankCode.class);
		arp.addMapping("t_bank_OreaCode", "bankOreaCode",BankOreaCode.class);
		arp.addMapping("t_recharge_trace", "traceCode",RechargeTrace.class);
		arp.addMapping("t_notice", "id",Notice.class);
		arp.addMapping("t_share", "uid",Share.class);
		arp.addMapping("t_recommend_info", "rid",RecommendInfo.class);
		arp.addMapping("t_recommend_reward", "rewardId",RecommendReward.class);
		arp.addMapping("t_market", "mid",Market.class);
		arp.addMapping("t_market_user", "id",MarketUser.class);
		arp.addMapping("t_sms_log", "id",SMSLog.class);
		arp.addMapping("t_slider_pic", "id", SliderPic.class);
		arp.addMapping("view_syscount", ViewSysCount.class);
		arp.addMapping("t_sys_config", "cfgCode",SysConfig.class);
		arp.addMapping("t_auth_log", "id",AuthLog.class);
		arp.addMapping("t_banks_v2", "userCode",BanksV2.class);
		arp.addMapping("t_auto_loan_v2", "aid",AutoLoan_v2.class);
		arp.addMapping("t_tickets", "tCode",Tickets.class);
		arp.addMapping("t_short_password", "mid",ShortPassword.class);
		arp.addMapping("t_prizes", "prizeCode", Prizes.class);
		arp.addMapping("t_prize_records", "recordCode",PrizeRecords.class);
		arp.addMapping("temp_report", "userCode",TempReport.class);
		arp.addMapping("t_share_award", "id",ShareAward.class);
		arp.addMapping("t_auto_map", "aid",AutoMap.class);
		arp.addMapping("t_cfca_info", "id",CFCAInfo.class);
		arp.addMapping("t_user_count", "countDate", UserCount.class);
		arp.addMapping("t_flt_userinfo", "userCode", FanLiTouUserInfo.class);
		arp.addMapping("t_sign_trace", "traceCode", SignTrace.class);
		arp.addMapping("t_changebank_trace","uid",ChangeBankTrace.class);
		arp.addMapping("t_settlement_early", "loanCode", SettlementEarly.class);
		arp.addMapping("t_fuiou_trace", "traceCode",FuiouTrace.class);//存管交易流水 2017.6.9
		arp.addMapping("t_contract_template", "templateCode", ContractTemplate.class);	// 合同模板
		arp.addMapping("t_contracts", "contractCode", Contracts.class);	// 合同列表
		arp.addMapping("t_redeem", "redeemCode", Redeem.class);	// 兑换码列表 ws 20171123
		arp.addMapping("t_loan_nxjd", "uid", LoanNxjd.class);
		arp.addMapping("t_questionnaire_records","tid",QuestionnaireRecords.class);
		arp.addMapping("t_question","qCode",Question.class);//客服问题 ws 20180228
		arp.addMapping("t_leave_note","leaveNoteCode",LeaveNote.class);//留言 ws 20180307
		arp.addMapping("t_loan_repayment", "rid", LoanRepayment.class);	// 回款计划
		arp.addMapping("t_jx_trace", "jxTraceCode", JXTrace.class);
		arp.addMapping("t_returned_amount", "uid",ReturnedAmount.class);//待还金额
		arp.addMapping("t_yistage_userinfo", "userCode",YiStageUserInfo.class);//易分期用户信息
		arp.addMapping("t_history_recy", "recyCode",HistoryRecy.class);
		arp.addMapping("t_yistage_trace", "traceCode", YistageTrace.class);
		arp.addMapping("t_user_terms_auth", "userCode", UserTermsAuth.class);	// 用户存管授权信息
		arp.addMapping("t_transfer_way", "userCode",TransferWay.class);
	}
}
