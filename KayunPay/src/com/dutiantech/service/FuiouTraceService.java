package com.dutiantech.service;

import java.util.List;

import com.dutiantech.controller.FuiouController;
import com.dutiantech.model.FuiouTrace;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.FreezeReqData;
import com.fuiou.data.QueryBalanceReqData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.QueryBalanceRspData;
import com.fuiou.data.QueryUserInfsReqData;
import com.fuiou.data.QueryUserInfsRspData;
import com.fuiou.data.QueryUserInfsRspDetailData;
import com.fuiou.data.TransferBmuReqData;
import com.fuiou.data.UnFreezeRspData;
import com.fuiou.service.FuiouService;

public class FuiouTraceService extends BaseService {
	
	public static final FuiouTrace fuiouTraceDao = new FuiouTrace();
	
	/**
	 * 保存Fuiou流水
	 * @param traceCode	交易流水号
	 * @param fuiouId	存管ID
	 * @param userCode	操作用户ID
	 * @param amount	操作金额
	 * @param fuiouTraceType	操作类型
	 * @return
	 */
	public boolean save(String traceCode, String fuiouId, String userCode, String amount, FuiouTraceType fuiouTraceType) {
		FuiouTrace fuiouTrace = new FuiouTrace();
		fuiouTrace.set("traceCode", traceCode);
		fuiouTrace.set("InLoginId", fuiouId);
		fuiouTrace.set("InUserCode", userCode);
		fuiouTrace.set("amount", amount);
		fuiouTrace.set("traceDate", DateUtil.getNowDateTime());
		fuiouTrace.set("traceType", fuiouTraceType.val());
		fuiouTrace.set("traceTypeName", fuiouTraceType.desc());
		return fuiouTrace.save();
	}
	public boolean save(String traceCode, String outFuiouId, String outuserCode,String inFuiouId, String inuserCode, String amount, FuiouTraceType fuiouTraceType,String loanCode,String remark){
		FuiouTrace fuiouTrace = new FuiouTrace();
		fuiouTrace.set("traceCode", traceCode);
		fuiouTrace.set("OutLoginId", outFuiouId);
		fuiouTrace.set("OutUserCode", outuserCode);
		fuiouTrace.set("InLoginId", inFuiouId);
		fuiouTrace.set("InUserCode", inuserCode);
		fuiouTrace.set("amount", amount);
		fuiouTrace.set("traceDate", DateUtil.getNowDateTime());
		fuiouTrace.set("traceType", fuiouTraceType.val());
		fuiouTrace.set("traceTypeName", fuiouTraceType.desc());
		fuiouTrace.set("loanCode", loanCode);
		fuiouTrace.set("remark", remark);
		return fuiouTrace.save();
	}
	/**
	 * 存管解冻  20170602
	 * 
	 * */
	public UnFreezeRspData unFreeFunds(User user,long amount){
		String loginid;
		UnFreezeRspData unFreezeRspData =null;
		try {
			loginid = CommonUtil.decryptUserMobile(user.getStr("loginId"));
			FreezeReqData freezeReqData=new FreezeReqData();
			freezeReqData.setVer(FuiouController.VER);
			freezeReqData.setCust_no(loginid);
			freezeReqData.setAmt(String.valueOf(amount));//解冻金额
			freezeReqData.setMchnt_cd(FuiouController.MCHNT_CD);//商户
			String ssn=CommonUtil.genMchntSsn();//交易流水号
			freezeReqData.setMchnt_txn_ssn(ssn);
			unFreezeRspData = FuiouService.unFreeze(freezeReqData);
			if("0000".equals(unFreezeRspData.getResp_code())){
				FuiouTrace fuiouTrace= new FuiouTrace();
				fuiouTrace.set("traceCode", ssn);
				fuiouTrace.set("InLoginId", loginid);
				String userCode=user.getStr("userCode");
				fuiouTrace.set("InUserCode", userCode);
				fuiouTrace.set("amount", amount);
				fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
				fuiouTrace.set("traceType",FuiouTraceType.D.val());
				fuiouTrace.set("traceTypeName",FuiouTraceType.D.desc());
				fuiouTrace.save();
			}else{
				FuiouTrace fuiouTrace= new FuiouTrace();
				fuiouTrace.set("traceCode", ssn);
				fuiouTrace.set("InLoginId", loginid);
				String userCode=user.getStr("userCode");
				fuiouTrace.set("InUserCode", userCode);
				fuiouTrace.set("amount", amount);
				fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
				fuiouTrace.set("traceType",FuiouTraceType.L.val());
				fuiouTrace.set("traceTypeName",FuiouTraceType.L.desc());
				fuiouTrace.save();
			}
		} catch (Exception e) {
			return unFreezeRspData;
		}
		return unFreezeRspData;
	}
	
	  /**
	   * 存管冻结  2017.5.26 rain  
	   * */
   public CommonRspData  freeze(User user,long amount){
	String loginid;
	CommonRspData commonRspData=null;
	try {
		loginid = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		FreezeReqData freezeReqData=new FreezeReqData();
		freezeReqData.setVer(FuiouController.VER);
		freezeReqData.setCust_no(loginid);
		freezeReqData.setAmt(String.valueOf(amount));//解冻金额
		freezeReqData.setMchnt_cd(FuiouController.MCHNT_CD);//商户
		String ssn=CommonUtil.genMchntSsn();//交易流水号
		freezeReqData.setMchnt_txn_ssn(ssn);
		commonRspData = FuiouService.freeze(freezeReqData);
		if("0000".equals(commonRspData.getResp_code())){
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("InLoginId", loginid);
			String userCode=user.getStr("userCode");
			fuiouTrace.set("InUserCode", userCode);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
			fuiouTrace.set("traceType",FuiouTraceType.C.val());
			fuiouTrace.set("traceTypeName", FuiouTraceType.C.desc());
			fuiouTrace.save();
		}else{
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("InLoginId", loginid);
			String userCode=user.getStr("userCode");
			fuiouTrace.set("InUserCode", userCode);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
			fuiouTrace.set("traceType", FuiouTraceType.K.val());
			fuiouTrace.set("traceTypeName",FuiouTraceType.K.desc());
			fuiouTrace.save();
		}
	} catch (Exception e) {
		return commonRspData;
	}
	return commonRspData;

	}
   /**
    * 划拨接口  ws
    * payAmount金额
    * tt 类型
    * Outuser 出账户
    * Inuser 进账户
    * */
	public CommonRspData  refund(Long payAmount,FuiouTraceType tt,User Outuser,User Inuser){
		CommonRspData comm=null;
		String amount = String.valueOf(payAmount);
		String userCode=Outuser.getStr("userCode");
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(CommonUtil.VER);
		String loginId=null;
		String loginIdt=null;
		try {
			loginId = CommonUtil.decryptUserMobile(Outuser.getStr("loginId"));
			loginIdt = CommonUtil.decryptUserMobile(Inuser.getStr("loginId"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String ssn=CommonUtil.genMchntSsn();
		transfe.setMchnt_txn_ssn(ssn);
		transfe.setMchnt_cd(CommonUtil.MCHNT_CD);
		transfe.setOut_cust_no(loginId);
		transfe.setIn_cust_no(loginIdt);
		try {
			 comm=FuiouService.transferBu(transfe);
			//记录流水
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime());
			fuiouTrace.set("OutLoginId", loginId);
			fuiouTrace.set("OutUserCode", userCode);
			fuiouTrace.set("InLoginId", loginIdt);
			fuiouTrace.set("InUserCode", Inuser.getStr("userCode"));
			if("0000".equals(comm.getResp_code())){
				fuiouTrace.set("traceType",tt.val());
				fuiouTrace.set("traceTypeName",tt.desc());	
			}else{
				if(tt.val().equals(FuiouTraceType.R.val())){
					fuiouTrace.set("traceType",FuiouTraceType.S.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.S.desc());
				}else if(tt.val().equals(FuiouTraceType.T.val())){
					fuiouTrace.set("traceType",FuiouTraceType.U.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.U.desc());
				}else if(tt.val().equals(FuiouTraceType.V.val())){
					fuiouTrace.set("traceType",FuiouTraceType.W.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.W.desc());
				}else if(tt.val().equals(FuiouTraceType.F.val())){
					fuiouTrace.set("traceType",FuiouTraceType.N.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.N.desc());
				}else if(tt.val().equals(FuiouTraceType.E.val())){
					fuiouTrace.set("traceType",FuiouTraceType.M.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.M.desc());
				}else if(tt.val().equals(FuiouTraceType.I.val())){
					fuiouTrace.set("traceType",FuiouTraceType.J.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.J.desc());
				}else if(tt.val().equals(FuiouTraceType.G.val())){
					fuiouTrace.set("traceType",FuiouTraceType.H.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.H.desc());
				}else if(tt.val().equals(FuiouTraceType.X.val())){
					fuiouTrace.set("traceType",FuiouTraceType.Y.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.Y.desc());
				}else if(tt.val().equals(FuiouTraceType.HKL.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKLE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKLE.desc());
				}else if(tt.val().equals(FuiouTraceType.HKB.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKBE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKBE.desc());
				}else{
					fuiouTrace.set("traceType",FuiouTraceType.Z.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.Z.desc());
				}
			}
			fuiouTrace.save();
		} catch (Exception e) {
			System.out.println(loginId+"用户划拨系统错误");
		}
		return comm;
	}
	
	//传递标编码的划拨接口
	public CommonRspData  refund(Long payAmount,FuiouTraceType tt,User Outuser,User Inuser,String loanCode){
		CommonRspData comm=null;
		String amount = String.valueOf(payAmount);
		String userCode=Outuser.getStr("userCode");
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(FuiouController.VER);
		String loginId=null;
		String loginIdt=null;
		try {
			loginId = CommonUtil.decryptUserMobile(Outuser.getStr("loginId"));
			loginIdt = CommonUtil.decryptUserMobile(Inuser.getStr("loginId"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		String ssn=CommonUtil.genMchntSsn();
		transfe.setMchnt_txn_ssn(ssn);
		transfe.setMchnt_cd(FuiouController.MCHNT_CD);
		transfe.setOut_cust_no(loginId);
		transfe.setIn_cust_no(loginIdt);
		try {
			 comm=FuiouService.transferBu(transfe);
			//记录流水
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime());
			fuiouTrace.set("OutLoginId", loginId);
			fuiouTrace.set("OutUserCode", userCode);
			fuiouTrace.set("InLoginId", loginIdt);
			fuiouTrace.set("InUserCode", Inuser.getStr("userCode"));
			fuiouTrace.set("loanCode", loanCode);
			if("0000".equals(comm.getResp_code())){
				fuiouTrace.set("traceType",tt.val());
				fuiouTrace.set("traceTypeName",tt.desc());	
			}else{
				if(tt.val().equals(FuiouTraceType.R.val())){
					fuiouTrace.set("traceType",FuiouTraceType.S.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.S.desc());
				}else if(tt.val().equals(FuiouTraceType.T.val())){
					fuiouTrace.set("traceType",FuiouTraceType.U.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.U.desc());
				}else if(tt.val().equals(FuiouTraceType.V.val())){
					fuiouTrace.set("traceType",FuiouTraceType.W.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.W.desc());
				}else if(tt.val().equals(FuiouTraceType.F.val())){
					fuiouTrace.set("traceType",FuiouTraceType.N.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.N.desc());
				}else if(tt.val().equals(FuiouTraceType.E.val())){
					fuiouTrace.set("traceType",FuiouTraceType.M.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.M.desc());
				}else if(tt.val().equals(FuiouTraceType.I.val())){
					fuiouTrace.set("traceType",FuiouTraceType.J.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.J.desc());
				}else if(tt.val().equals(FuiouTraceType.G.val())){
					fuiouTrace.set("traceType",FuiouTraceType.H.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.H.desc());
				}else if(tt.val().equals(FuiouTraceType.X.val())){
					fuiouTrace.set("traceType",FuiouTraceType.Y.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.Y.desc());
				}else if(tt.val().equals(FuiouTraceType.HKL.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKLE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKLE.desc());
				}else if(tt.val().equals(FuiouTraceType.HKB.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKBE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKBE.desc());
				}else{
					fuiouTrace.set("traceType",FuiouTraceType.Z.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.Z.desc());
				}
			}
			fuiouTrace.save();
		} catch (Exception e) {
			System.out.println(loginId+"用户划拨系统错误");
		}
		return comm;
	}
	/**
	 * 
	 * 存管转账  20170609 ws
	 * payAmount  划拨金额
	 * user       用户信息
	 * tt         交易类型
	 * */
	public CommonRspData  gorefund(Long payAmount,User user,FuiouTraceType tt){
		CommonRspData comm=null;
		String amount = String.valueOf(payAmount);
		String userCode=user.getStr("userCode");
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(CommonUtil.VER);
		String login_id=null;
		try {
			login_id = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		transfe.setMchnt_cd(CommonUtil.MCHNT_CD);
		if(tt.val().equals(FuiouTraceType.E.val())){
			transfe.setOut_cust_no(CommonUtil.fUIOUACCOUNT);
			transfe.setIn_cust_no(login_id);
		}
		if(tt.val().equals(FuiouTraceType.F.val())){
			transfe.setOut_cust_no(login_id);
			transfe.setIn_cust_no(CommonUtil.fUIOUACCOUNT);
		}
		if(tt.val().equals(FuiouTraceType.HKL.val())||tt.val().equals(FuiouTraceType.HKB.val())){
			transfe.setOut_cust_no(CommonUtil.fUIOUACCOUNT);
			transfe.setIn_cust_no(login_id);
		}
		if(tt.val().equals(FuiouTraceType.X.val())){
			transfe.setOut_cust_no(CommonUtil.fUIOUACCOUNT);
			transfe.setIn_cust_no(login_id);
		}
		String ssn=CommonUtil.genMchntSsn();
		transfe.setMchnt_txn_ssn(ssn);
		try {
			 comm=FuiouService.transferBmu(transfe);
			//记录流水
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("InLoginId", login_id);
			fuiouTrace.set("InUserCode", userCode);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
			if("0000".equals(comm.getResp_code())){
				fuiouTrace.set("traceType",tt.val());
				fuiouTrace.set("traceTypeName",tt.desc());
				
			}else{
				if(tt.val().equals(FuiouTraceType.E.val())){
					fuiouTrace.set("traceType",FuiouTraceType.M.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.M.desc());
				}
				if(tt.val().equals(FuiouTraceType.F.val())){
					fuiouTrace.set("traceType",FuiouTraceType.N.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.N.desc());
				}
				if(tt.val().equals(FuiouTraceType.HKL.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKLE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKLE.desc());
				}
				if(tt.val().equals(FuiouTraceType.HKB.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKBE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKBE.desc());
				}
				if(tt.val().equals(FuiouTraceType.X.val())){
					fuiouTrace.set("traceType",FuiouTraceType.Y.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.Y.desc());
				}
			}
			fuiouTrace.save();
		} catch (Exception e) {
			System.out.println(login_id+"用户划拨系统错误");
		}
		return comm;
	}
	//	传标号的转账接口
	public CommonRspData  gorefund(Long payAmount,User user,FuiouTraceType tt,String loanCode){
		CommonRspData comm=null;
		String amount = String.valueOf(payAmount);
		String userCode=user.getStr("userCode");
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(CommonUtil.VER);
		String login_id=null;
		try {
			login_id = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		transfe.setMchnt_cd(CommonUtil.MCHNT_CD);
		if(tt.val().equals(FuiouTraceType.E.val())){
			transfe.setOut_cust_no(CommonUtil.fUIOUACCOUNT);
			transfe.setIn_cust_no(login_id);
		}
		if(tt.val().equals(FuiouTraceType.F.val())){
			transfe.setOut_cust_no(login_id);
			transfe.setIn_cust_no(CommonUtil.fUIOUACCOUNT);
		}
		if(tt.val().equals(FuiouTraceType.HKL.val())||tt.val().equals(FuiouTraceType.HKB.val())){
			transfe.setOut_cust_no(CommonUtil.fUIOUACCOUNT);
			transfe.setIn_cust_no(login_id);
		}
		
		String ssn=CommonUtil.genMchntSsn();
		transfe.setMchnt_txn_ssn(ssn);
		try {
			 comm=FuiouService.transferBmu(transfe);
			//记录流水
			FuiouTrace fuiouTrace= new FuiouTrace();
			fuiouTrace.set("traceCode", ssn);
			fuiouTrace.set("InLoginId", login_id);
			fuiouTrace.set("InUserCode", userCode);
			fuiouTrace.set("amount", amount);
			fuiouTrace.set("traceDate",DateUtil.getNowDateTime() );
			fuiouTrace.set("loanCode", loanCode);
			if("0000".equals(comm.getResp_code())){
				fuiouTrace.set("traceType",tt.val());
				fuiouTrace.set("traceTypeName",tt.desc());
				
			}else{
				if(tt.val().equals(FuiouTraceType.E.val())){
					fuiouTrace.set("traceType",FuiouTraceType.M.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.M.desc());
				}
				if(tt.val().equals(FuiouTraceType.F.val())){
					fuiouTrace.set("traceType",FuiouTraceType.N.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.N.desc());
				}
				if(tt.val().equals(FuiouTraceType.HKL.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKLE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKLE.desc());
				}
				if(tt.val().equals(FuiouTraceType.HKB.val())){
					fuiouTrace.set("traceType",FuiouTraceType.HKBE.val());
					fuiouTrace.set("traceTypeName",FuiouTraceType.HKBE.desc());
				}
			}
			fuiouTrace.save();
		} catch (Exception e) {
			System.out.println(login_id+"用户划拨系统错误");
		}
		return comm;
	}
	/**
	 * 存管流水记录 
	 * 可记录冻结、解冻、充值、提现、转账（用户与商户之间）
	 * ssn     存管流水号
	 * user    用户信息
	 * amount  金额
	 * tt      交易类型
	 * */
	public   boolean fuiouTraceContent(String ssn,User user,long amount,FuiouTraceType tt){
		FuiouTrace fuiouTrace= new FuiouTrace();
		String login_id=null;
		try {
			login_id = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userCode=user.getStr("userCode");
		fuiouTrace.set("traceCode", ssn);
		fuiouTrace.set("InLoginId", login_id);
		fuiouTrace.set("InUserCode", userCode);
		fuiouTrace.set("amount", amount);
		fuiouTrace.set("traceDate",DateUtil.getNowDateTime());
		switch (tt) {
		case RECHARGE:
			tt = FuiouTraceType.A;
			break;
		case WITHDRAW:
			tt = FuiouTraceType.B;
			break;
		default:
			break;
		}
		fuiouTrace.set("traceType",tt.val());
		fuiouTrace.set("traceTypeName",tt.desc());
		return fuiouTrace.save();
	}
	
	public boolean fuiouTraceError(String ssn, User user, long amount, FuiouTraceType fuiouTraceType) {
		FuiouTrace fuiouTrace= new FuiouTrace();
		String login_id=null;
		try {
			login_id = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userCode=user.getStr("userCode");
		fuiouTrace.set("traceCode", ssn);
		fuiouTrace.set("InLoginId", login_id);
		fuiouTrace.set("InUserCode", userCode);
		fuiouTrace.set("amount", amount);
		fuiouTrace.set("traceDate",DateUtil.getNowDateTime());
		switch (fuiouTraceType) {
			case RECHARGE:
				fuiouTraceType = FuiouTraceType.O;
				break;
			case WITHDRAW:
				fuiouTraceType = FuiouTraceType.P;
				break;
			default:
				break;
		}
		fuiouTrace.set("traceType",fuiouTraceType.val());
		fuiouTrace.set("traceTypeName",fuiouTraceType.desc());
		return fuiouTrace.save();
	}
	/**
	 * 查询存管余额
	 * user    用户信息
	 * */
	public QueryBalanceResultData BalanceFunds(User user){
		String loginid=null;
		QueryBalanceRspData queryBalanceRspData=null;
		try {
			loginid= CommonUtil.decryptUserMobile(user.getStr("loginId"));
			QueryBalanceReqData queryBalanceReqData =new QueryBalanceReqData();
			queryBalanceReqData.setCust_no(loginid);
			queryBalanceReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			String mchnt_txn_ssn=CommonUtil.genMchntSsn();//交易流水号
			queryBalanceReqData.setMchnt_txn_ssn(mchnt_txn_ssn);
			queryBalanceReqData.setMchnt_txn_dt(DateUtil.getNowDate());
			queryBalanceRspData=FuiouService.balanceAction(queryBalanceReqData);
		} catch (Exception e2) {
			return new QueryBalanceResultData();
		}
		QueryBalanceResultData queryB =queryBalanceRspData.getResults().get(0);
		return queryB;
	}
	/**
	 * 验证用户是否开通存管
	 * */
	public boolean isfuiouAcount(User user){
		String loginId = user.getStr("loginId");
		try {
			loginId = CommonUtil.decryptUserMobile(loginId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		QueryUserInfsReqData queryUserInfsReqData=new QueryUserInfsReqData();
		queryUserInfsReqData.setVer(FuiouController.VER);
		queryUserInfsReqData.setMchnt_cd(FuiouController.MCHNT_CD);
		queryUserInfsReqData.setMchnt_txn_dt(DateUtil.getNowDate());
		queryUserInfsReqData.setMchnt_txn_ssn(CommonUtil.genMchntSsn());
		queryUserInfsReqData.setUser_ids(loginId);
		try {
			QueryUserInfsRspData queryUserInfsRspData =FuiouService.queryUserInfs(queryUserInfsReqData);
			List<QueryUserInfsRspDetailData> qqqDatas=queryUserInfsRspData.getResults();
			if(null==qqqDatas.get(0).getMobile_no()||"".equals(qqqDatas.get(0).getMobile_no())){
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	
	
}
