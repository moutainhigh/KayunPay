package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceSynState;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.util.UIDUtil;
import com.fuiou.data.QueryBalanceResultData;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jx.util.RetCodeUtil;
public class FundsServiceV2 extends BaseService{

   //private FundsTraceService traceService = new FundsTraceService() ;
	
	/**
	 * 存管资金校验
	 * @param user
	 * @return	true-同步; false-不同步
	 */
	public boolean checkBalance(User user) {
		// 获取用户平台可用、冻结资金
		Funds funds = Funds.fundsDao.findFirst("SELECT * FROM t_funds WHERE userCode = ?", user.getStr("userCode"));
		long avBalance = funds.getLong("avBalance");
		long frozeBalance = funds.getLong("frozeBalance");
		
		long depositAvBalance = 0l;
		long depositFrozeBalance = 0l;
		// 获取用户存管户可用、冻结资金
		if (CommonUtil.jxPort) {
			Map<String, String> depositBalance = JXQueryController.balanceQuery(user.getStr("jxAccountId"));
			Map<String, String> depositFreezeBalance = JXQueryController.freezeAmtQuery(user.getStr("jxAccountId"));
			if (null == depositBalance || null == depositFreezeBalance) {
				return false;
			}
			if (!RetCodeUtil.isSuccRetCode(depositBalance.get("retCode")) || !RetCodeUtil.isSuccRetCode(depositFreezeBalance.get("retCode"))) {
				throw new BaseBizRunTimeException("99", "存管接口查询失败", "");
			}
			depositAvBalance = StringUtil.getMoneyCent(depositBalance.get("availBal"));
			depositFrozeBalance = StringUtil.getMoneyCent(depositFreezeBalance.get("bidAmt")) + 
					StringUtil.getMoneyCent(depositFreezeBalance.get("repayAmt")) + 
					StringUtil.getMoneyCent(depositFreezeBalance.get("plAmt"));
		} else if (CommonUtil.fuiouPort) {
			QueryBalanceResultData fuiouFunds = FuiouController.balanceQuery(user.getStr("loginId"));
			depositAvBalance = Long.parseLong(fuiouFunds.getCa_balance());
			depositFrozeBalance = Long.parseLong(fuiouFunds.getCf_balance());
		} else {
			throw new BaseBizRunTimeException("99", "存管接口查询失败", "");
		}
		
		// 验证资金是否同步
		if (avBalance == depositAvBalance) {	// 判断可用金额是否同步
			if (frozeBalance == depositFrozeBalance) {	// 判断冻结金额是否同步
				return true;
			} else {
				// 获取用户投标冻结金额
				long bidAmount = Db.queryBigDecimal("SELECT COALESCE(SUM(payAmount), 0) FROM t_loan_trace WHERE payUserCode = ? AND loanState = 'J'", user.getStr("userCode")).longValue();
				// 获取用户回款,批次债转冻结金额（1日内）
				long recyAmount = Db.queryBigDecimal("select COALESCE(sum(if(traceType in ('R','L','B'),traceAmount-traceFee,0)),0)-COALESCE(sum(if(traceType='Y',traceAmount-traceFee,0)),0) from t_funds_trace where userCode=? and traceDate>DATE_SUB(NOW(),INTERVAL 1 DAY) and traceRemark like '%解冻%'",user.getStr("userCode")).longValue();
				if((frozeBalance - bidAmount - recyAmount) == depositFrozeBalance){
					return true;
				}
			}
		}else {
			// 获取用户回款,批次债转冻结金额（1日内）
			long recyAmount = Db.queryBigDecimal("select COALESCE(sum(if(traceType in ('R','L','B'),traceAmount-traceFee,0)),0)-COALESCE(sum(if(traceType='Y',traceAmount-traceFee,0)),0) from t_funds_trace where userCode=? and traceDate>DATE_SUB(NOW(),INTERVAL 1 DAY) and traceRemark like '%解冻%'",user.getStr("userCode")).longValue();
			if(recyAmount > 0 && (avBalance+recyAmount == depositAvBalance)){
				return true;
			}
		}
		return false;
	}
	
	public Funds findById(String userCode){
		return Funds.fundsDao.findById(userCode);
	}
	/**
	 * 同步可用余额和冻结余额
	 * @param userCode
	 * @param doAmount
	 * @return
	 */
	public Funds syncAccount(String userCode, long avBalance,long frozeBalance ){
	
		String upSql = "update t_funds set  avBalance=?,frozeBalance=?,updateDateTime=?,"
				+ "updateDate=?,updateTime=? where userCode=? " ;
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		List<Object> ps = new ArrayList<Object>() ;
		ps.add(avBalance);
		ps.add(frozeBalance);
		ps.add(nowDate+nowTime) ;
		ps.add(nowDate) ;
		ps.add(nowTime) ;
		ps.add(userCode) ;
		Funds funds = null ;
		
			funds = getFundsByUserCode(userCode) ;
			int result = Db.update(upSql, ps.toArray());
		if( result > 0 ){
			//修改成功
			funds = getFundsByUserCode(userCode);
		}else{
			//修改失败
			throw new BaseBizRunTimeException("B6", "余额操作失败!", null ) ;
		}
		
		return funds ;
	}
	/**
	 * 	可用资金转冻结资金，非业务接口，不记录流水
	 * @return		账户当前可用余额
	 * 		B1 - 账户余额不足
	 * 		B2 - 同步更新失败,失败原因可能是可用余额不足
	 */
	public Funds avBalance2froze(String userCode , long doAmount ){
		Funds funds = getFundsByUserCode(userCode);
		long avBalance = funds.getLong("avBalance") ;
		if( avBalance < doAmount ){
			throw new BaseBizRunTimeException("B1", "账户余额不足!", avBalance ) ;
		}
		
		String upSql = "update t_funds set avBalance=avBalance-?,frozeBalance=frozeBalance+?,updateDateTime=?,"
				+ "updateDate=?,updateTime=?"
				+ " where userCode=? and (avBalance-?)>=0";
		
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String nowDateTime = nowDate + nowTime ;
		
		int result = Db.update(upSql, doAmount , doAmount , nowDateTime , nowDate , nowTime , userCode , doAmount ) ;
		if( result > 0 ){
			//更新成功
			funds = getFundsByUserCode(userCode);
		}else{
			//更新失败
			throw new BaseBizRunTimeException("B2", "账户可用余额转冻结余额异常，可用余额不足或其他原因!", funds.getLong("avBalance") ) ;
		}
		return funds ;
	}
	
	/**
	 * 	冻结余额转可用余额，非业务接口，不记录流水
	 * @param userCode
	 * @param doAmount
	 * @return
	 */
	public Funds frozeBalance2avBalance(String userCode , long doAmount){

		Funds funds = getFundsByUserCode(userCode);
		long frozeBalance = funds.getLong("frozeBalance") ;
		if( frozeBalance < doAmount ){
			throw new BaseBizRunTimeException("B3", "账户"+userCode+"【"+funds.getStr("userName")+"】冻结余额不足!", frozeBalance ) ;
		}
		String upSql = "update t_funds set avBalance=avBalance+?,frozeBalance=frozeBalance-?,updateDateTime=?,"
				+ "updateDate=?,updateTime=?"
				+ " where userCode=? and (frozeBalance-?)>=0";
		
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String nowDateTime = nowDate + nowTime ;
		
		int result = Db.update(upSql, doAmount , doAmount , nowDateTime , nowDate , nowTime , userCode , doAmount ) ;
		if( result > 0 ){
			//更新成功
			funds = getFundsByUserCode(userCode);
		}else{
			//更新失败
			throw new BaseBizRunTimeException("B4", "账户冻结余额转可用余额异常，冻结余额不足或其他原因!", funds.getLong("frozeBalance") ) ;
		}
		return funds ;
	}
	
	/**
	 * 	操作可用余额
	 * @param userCode
	 * @param opType	
	 * 	 0 - 增加
	 * 	 1 - 减少
	 * @param doAmount
	 * @return
	 */
	public Funds doAvBalance(String userCode , int opType , long doAmount ){
		String opChar = "-";
		if( opType == 0 ){
			opChar = "+";
		}
		String upSql = "update t_funds set  avBalance=avBalance" + opChar + "? ,updateDateTime=?,"
				+ "updateDate=?,updateTime=? where userCode=? " ;
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		List<Object> ps = new ArrayList<Object>() ;
		ps.add(doAmount) ;
		ps.add(nowDate+nowTime) ;
		ps.add(nowDate) ;
		ps.add(nowTime) ;
		ps.add(userCode) ;
		Funds funds = null ;
		if( opType == 1 ){
			funds = getFundsByUserCode(userCode) ;
			long avBalance = funds.getLong("avBalance") ;
			if( avBalance < doAmount ){
				throw new BaseBizRunTimeException("B5", userCode+"【"+funds.getStr("userName")+"】"+"可用余额不足[" + avBalance + "]", avBalance ) ;
			}
			//兼容
			ps.add(doAmount) ;
			upSql += " and (avBalance-?)>=0 " ;
		}
		
		int result = Db.update(upSql, ps.toArray() ) ;
		if( result > 0 ){
			//修改成功
			funds = getFundsByUserCode(userCode);
		}else{
			//修改失败
			throw new BaseBizRunTimeException("B6", "可用余额操作失败!", null ) ;
		}
		
		return funds ;
	}
	
	/**
	 * 人工提现
	 * @param userCode		用户编码
	 * @param payAmount		交易金额
	 * @param traceFee		额外交易费用
	 * @return
	 */
	public boolean withdrawals1(String userCode, Long payAmount, Integer traceFee, String traceRemark){
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode,"userCode,avBalance,frozeBalance,updateDate,updateTime");
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");
		if(avBalance<payAmount){
			return false;
		}
		//init
//		return updateFunds4user(userCode, traceType.G ,fundsType.D, 0-payAmount , 0 , 0 , traceRemark , false ) ;
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",SysEnum.traceType.E.val());//交易类型
		fundsTrace.set("traceTypeName",SysEnum.traceType.E.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.D.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance-payAmount);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//冻结余额
		fundsTrace.set("traceFee",traceFee);
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark);
		fundsTrace.set("traceMac", "");
		
		if(fundsTrace.save()){
			int x = Db.update("update t_funds set avBalance = avBalance - ?,totalWithdraw = totalWithdraw + ?,updateDateTime = ?,updateDate = ?,updateTime = ? where userCode=?",payAmount,payAmount,DateUtil.getNowDateTime(),DateUtil.getNowDate(),DateUtil.getNowTime(),userCode);
			if(x>0){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 处理提现申请，成功提现后 正式扣除申请提现的冻结金额(成功!)
	 * @param userCode		用户编码
	 * @param payAmount		交易金额
	 * @param traceFee		额外交易费用
	 * @return
	 */
	public boolean withdrawals3_ok(String userCode, Long payAmount, Integer traceFee, String traceRemark,SysEnum.traceType x2){
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode,"userCode,avBalance,frozeBalance,updateDate,updateTime");
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");
		//init
//		return updateFunds4user(userCode, traceType.G ,fundsType.D, 0-payAmount , 0 , 0 , traceRemark , false ) ;
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",x2.val());//交易类型
		fundsTrace.set("traceTypeName",x2.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.D.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance-payAmount);//冻结余额
		fundsTrace.set("traceFee",traceFee);
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark);
		fundsTrace.set("traceMac", "");
		
		if(fundsTrace.save()){
			int x = Db.update("update t_funds set frozeBalance = frozeBalance - ?,updateDateTime = ?,updateDate = ?,updateTime = ? where userCode=?",payAmount,DateUtil.getNowDateTime(),DateUtil.getNowDate(),DateUtil.getNowTime(),userCode);
			if(x>0){
				return true;
			}
		}
		return false;
	}
	public static void main(String[] args) {
		
		System.out.println(UIDUtil.generate());
	}
	
	/**
	 * 第三方支付 提现  回调通知成功后正式扣除申请提现的冻结金额(失败了!)
	 * @param userCode		用户编码
	 * @param payAmount		交易金额
	 * @param isScore		是否抵扣积分
	 * @return
	 */
	public boolean withdrawals3_buok(String userCode, Long payAmount, String isScore, String traceRemark){
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode,"userCode,avBalance,frozeBalance,updateDate,updateTime");
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");
//		return updateFunds4user(userCode, traceType.G ,fundsType.D, 0-payAmount , 0 , 0 , traceRemark , false ) ;
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",SysEnum.traceType.K.val());//交易类型
		fundsTrace.set("traceTypeName",SysEnum.traceType.K.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.J.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance+payAmount);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance-payAmount);//冻结余额
		fundsTrace.set("traceFee",0);
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark );
		fundsTrace.set("traceMac", "");
		if(isScore.equals("1")){
			doPoints(userCode, 0, 20000, "提现失败，补回积分");
		}
		if(fundsTrace.save()){
			int x = Db.update("update t_funds set avBalance = avBalance + ?,frozeBalance = frozeBalance - ?,updateDateTime = ?,updateDate = ?,updateTime = ?,totalWithdraw = totalWithdraw - ? where userCode=?",payAmount,payAmount,DateUtil.getNowDateTime(),DateUtil.getNowDate(),DateUtil.getNowTime(),payAmount,userCode);
			if(x>0){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 承接债权支出	递减可用余额
	 * @param userCode		用户编码
	 * @param transAmount	承接金额
	 * @param traceFee		承接手续费
	 * @return
	 */
	public boolean carryOnTransfer(String userCode , int transAmount,String traceRemark){
		Funds funds = getFundsByUserCode(userCode) ;
		Long avBalance = funds.getLong("avBalance");
		if(avBalance < transAmount){
			return false;
		}
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		
		//递减承接人可用余额
		doAvBalance(userCode, 1, transAmount);
		
		//添加扣款流水
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", user.getStr("userName"));
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("traceType", SysEnum.traceType.A.val());
		fundsTrace.set("traceTypeName", SysEnum.traceType.A.desc());
		fundsTrace.set("fundsType", SysEnum.fundsType.D.val());
		fundsTrace.set("traceAmount", transAmount);
		fundsTrace.set("traceBalance", avBalance - transAmount);
		fundsTrace.set("traceFrozeBalance", 0 );
		fundsTrace.set("traceFee", 0);
		fundsTrace.set("traceRemark", traceRemark);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceMac", "");
		return fundsTrace.save();
	}
	
	/**
	 * 转让债权收入	递加可用余额
	 * @param userCode		用户编码
	 * @param transAmount	转让金额
	 * @return
	 */
	public boolean carryOnTransferTo(String userCode , Long transAmount, Integer traceFee,Integer transFee,String traceRemark){
		Funds funds = getFundsByUserCode(userCode) ;
		Long avBalance = funds.getLong("avBalance");
//		if(avBalance < transAmount){
//			return false;
//		}
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		
		//递增转让人可用余额
		doAvBalance(userCode, 0, transAmount);
		//TODO 债权转让的手续费加到风险备用金
//		Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1",traceFee , DateUtil.getNowDate(),DateUtil.getNowTime());
		
		//添加扣款流水
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", user.getStr("userName"));
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("traceType", SysEnum.traceType.B.val());
		fundsTrace.set("traceTypeName", SysEnum.traceType.B.desc());
		fundsTrace.set("fundsType", SysEnum.fundsType.J.val());
		fundsTrace.set("traceAmount", transAmount);
		fundsTrace.set("traceBalance", avBalance + transAmount);
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance") );
		fundsTrace.set("traceFee", traceFee+transFee);
		fundsTrace.set("traceRemark", traceRemark);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceMac", "");
		return fundsTrace.save();
	}
	
	
	/**
	 *	申请提现，可用余额转冻结余额
	 * @param userCode		用户编码
	 * @param payAmount		交易金额
	 * @param traceFee		额外交易费用
	 * @return
	 */
	public boolean withdrawals2(String userCode, Long payAmount, Integer traceFee, String traceRemark){
		Funds funds = getFundsByUserCode(userCode) ;
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");
		if(avBalance<payAmount){
			return false;
		}
		//init
//		return updateFunds4user(userCode, traceType.G ,fundsType.D, 0-payAmount , 0 , 0 , traceRemark , false ) ;
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",SysEnum.traceType.F.val());//交易类型
		fundsTrace.set("traceTypeName",SysEnum.traceType.F.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.D.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance-payAmount);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance+payAmount);//冻结余额
		fundsTrace.set("traceFee",traceFee);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark);
		fundsTrace.set("traceMac", "");
		
		if(fundsTrace.save()){
//			int x = Db.update("update t_funds set avBalance = avBalance - ?,frozeBalance = frozeBalance + ?,updateDate = ?,updateTime = ? where userCode=?",payAmount,payAmount,DateUtil.getNowDate(),DateUtil.getNowTime(),userCode);
//			if(x>0){
//				return true;
//			}
			//未收手续费，提现成功时，扣除手续费，优先扣除积分
			funds = avBalance2froze(userCode, payAmount ) ;
			if( funds != null ){
				//增加提现总额
				updateTotalWithdraw(userCode, payAmount);
				return true ;
			}
		}
		return false;
	}
	

	/**
	 * 修改提现总额
	 * @param userCode
	 * @param payAmount
	 */
	public void updateTotalWithdraw(String userCode, Long payAmount){
		String sql = "update t_funds set totalWithdraw=totalWithdraw+? where userCode = ?";
		Db.update(sql, payAmount,userCode);
	}
	
	/**
	 * 	申请提现，归还冻结余额
	 * @param userCode
	 * @param payAmount
	 * @return
	 */
	public Funds withdrawals4funds(String userCode , long payAmount ){

		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;

		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", traceType.Y.val() );//交易类型
		fundsTrace.set("traceTypeName", traceType.Y.desc());//交易类型
		fundsTrace.set("fundsType",fundsType.J.val() );//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", funds.getLong("avBalance")+payAmount );//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance")-payAmount );//实时冻结余额
		fundsTrace.set("traceFee", 0 );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", "取消提现返还资金" );
		fundsTrace.set("traceMac", "");
		
		if( fundsTrace.save() ){
			funds = frozeBalance2avBalance(userCode, payAmount) ;
			updateTotalWithdraw(userCode, -payAmount);
		}
		
		return funds ;
	}
	
	
	
	/**
	 * 	操作冻结余额
	 * @param userCode
	 * @param opType	
	 * 	 0 - 增加
	 * 	 1 - 减少
	 * @param doAmount
	 * @return
	 * 
	 * 
	 */
	public Funds doFrozeBalance(String userCode , int opType , long doAmount ){
		String opChar = "-";
		if( opType == 0 ){
			opChar = "+";
		}
		String upSql = "update t_funds set  frozeBalance=frozeBalance" + opChar + "? ,updateDateTime=?,"
				+ "updateDate=?,updateTime=? where userCode=? " ;
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		List<Object> ps = new ArrayList<Object>() ;
		ps.add(doAmount) ;
		ps.add(nowDate+nowTime) ;
		ps.add(nowDate) ;
		ps.add(nowTime) ;
		ps.add(userCode) ;
	    Funds funds = null ;
		if( opType == 1 ){
			funds = getFundsByUserCode(userCode) ;
			long frozeBalance = funds.getLong("frozeBalance") ;
			if( frozeBalance < doAmount ){
				throw new BaseBizRunTimeException("B5", "冻结余额不足[" + frozeBalance + "分]"+userCode, frozeBalance ) ;
			}
			//兼容
//			upSql += " and (frozeBalance-?)>=0 " ;
		}
		
		int result = Db.update(upSql, ps.toArray() ) ;
		if( result > 0 ){
			//修改成功
			funds = getFundsByUserCode(userCode);
		}else{
			//修改失败
			throw new BaseBizRunTimeException("B6", "冻结余额操作失败!", null ) ;
		}
		
		return funds ;
	}
	
	/**
	 * 	操作消费积分
	 * @param userCode
	 * @param opType	
	 * 	 0 - 增加
	 * 	 1 - 减少
	 * @param doAmount
	 * @return
	 */
	public Funds doPoints(String userCode , int opType , long doAmount, String remark ){
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		List<Object> ps = new ArrayList<Object>() ;
		ps.add(doAmount) ;
		ps.add(nowDate+nowTime) ;
		ps.add(nowDate) ;
		ps.add(nowTime) ;
		ps.add(userCode) ;
		Funds funds = null ;
		long avpoints = 0;
		String opChar = "-";
		if( opType == 0 ){
			opChar = "+";
		}
		String upSql = "update t_funds set  points=points" + opChar + "? ,updateDateTime=?,"
				+ "updateDate=?,updateTime=? where userCode=? " ;
		if( opType == 1 ){
			funds = getFundsByUserCode(userCode) ;
			long points = funds.getLong("points") ;
			if( points < doAmount ){
				throw new BaseBizRunTimeException("B5", "可用积分不足[" + points/10/10 + "]", points/10/10 ) ;
			}
			//兼容
			upSql += " and (points-?)>=0 " ;
			ps.add(doAmount) ;
		}
		
		int result = Db.update(upSql, ps.toArray() ) ;
		if( result > 0 ){
			//修改成功
			funds = getFundsByUserCode(userCode);
			avpoints = funds.getLong("points");
		}else{
			//修改失败
			throw new BaseBizRunTimeException("B6", "可用积分不足,操作失败!", null ) ;
		}
		
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", User.userDao.findByIdLoadColumns(userCode, "userName").getStr("userName"));
		fundsTrace.set("traceType", opType==0?SysEnum.traceType.J.val():SysEnum.traceType.Z.val());
		fundsTrace.set("fundsType", opType==0?SysEnum.fundsType.J.val():SysEnum.fundsType.D.val());
		fundsTrace.set("traceAmount", doAmount);
		fundsTrace.set("traceBalance", funds.getLong("avBalance"));
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance"));
		fundsTrace.set("traceFee", 0);
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("traceDate", DateUtil.getNowDate());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceTime", DateUtil.getNowTime());
		fundsTrace.set("traceSynState", "N");
		fundsTrace.set("traceTypeName",opType==0?SysEnum.traceType.J.desc():SysEnum.traceType.Z.desc());//交易类型
		avpoints = CommonUtil.yunsuan(avpoints+"", ""+100, "chu", 0).longValue();
		fundsTrace.set("traceRemark", remark+",当前可用积分余额["+avpoints+"],");
		fundsTrace.save();
		return funds ;
	}
	
	/**
	 * 满标放款时更新理财人资金账户
	 * @param userCode
	 * @param payAmount
	 * @param points
	 * @return
	 */
	public Funds updateFundsWhenFullLoan(String userCode , long payAmount ,long points ){
		
		//Funds funds = getFundsByUserCode(userCode);
		//更新冻结账户
		Funds funds = doFrozeBalance(userCode, 1 , payAmount ) ; 
		
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceAmount", payAmount);
		fundsTrace.set("traceBalance",funds.getLong("avBalance"));
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance"));
		//新增资金流水
		boolean traceResult = addTrace(fundsTrace , SysEnum.traceType.P ,
				SysEnum.fundsType.D , "投标成功，投标金额在冻结余额中扣除") ;
		if ( traceResult == false ){
			funds = doFrozeBalance(userCode, 0 , payAmount ) ; 
			throw new BaseBizRunTimeException("B7", "更新账户失败[资金流水异常]", null ) ;
		}else{
			//资金流水处理完成,新增积分
			funds = doPoints(userCode, 0 , points,"投标获取积分") ;
		}
		return funds ;
	}
	
	/**
	 * 满标放款时更新理财人资金账户	WJW
	 * @param userCode
	 * @param userName
	 * @param jxAccountId
	 * @param payAmount
	 * @param points
	 * @return
	 */
	public Funds updateFundsWhenFullLoan(String userCode ,String userName,String jxAccountId ,long payAmount ,long points ){
		//更新冻结账户
		Funds funds = null;
		Map<String, String> accountBalance = JXQueryController.balanceQuery(jxAccountId);
		Map<String, String> freezeBalance = JXQueryController.freezeAmtQuery(jxAccountId);
		long avBalance = StringUtil.getMoneyCent(accountBalance.get("availBal"));
		long frozeBalance = StringUtil.getMoneyCent(freezeBalance.get("bidAmt")) + StringUtil.getMoneyCent(freezeBalance.get("repayAmt")) + StringUtil.getMoneyCent(freezeBalance.get("plAmt"));
		
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", userName);
		fundsTrace.set("traceAmount", payAmount);
		fundsTrace.set("traceBalance",avBalance);
		fundsTrace.set("traceFrozeBalance", frozeBalance);
		//新增资金流水
		boolean traceResult = addTrace(fundsTrace , SysEnum.traceType.P ,
				SysEnum.fundsType.D , "投标成功，投标金额在冻结余额中扣除") ;
		if ( traceResult == false ){
//			funds = doFrozeBalance(userCode, 0 , payAmount ) ; 
			throw new BaseBizRunTimeException("B7", "更新账户失败[资金流水异常]", null ) ;
		}else{
			//资金流水处理完成,新增积分
			funds = doPoints(userCode, 0 , points,"投标获取积分") ;
		}
		return funds ;
	}
	
	public boolean updateFundsWhenFullLoanWithTicket(String userCode , long ticketAmount ){
		
		Funds funds = getFundsByUserCode(userCode) ;
		//更新冻结账户
		
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceAmount", ticketAmount);
		fundsTrace.set("traceBalance",funds.getLong("avBalance"));
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance"));
		//新增资金流水
		boolean traceResult = addTrace(fundsTrace , SysEnum.traceType.N ,
				SysEnum.fundsType.D , "投标成功，使用现金抵用券抵扣共："+CommonUtil.yunsuan(ticketAmount+"", "100.00", "chu", 0).longValue()) ;
		return traceResult;
	}
	
	private boolean addTrace(FundsTrace fundsTrace , traceType tt , fundsType ft , String remark){
		//String userCode = fundsTrace.getStr("userCode");
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("traceType", tt.val());
		fundsTrace.set("traceTypeName", tt.desc());
		fundsTrace.set("fundsType", ft.val());
		fundsTrace.set("traceFee", 0 );
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceDate", DateUtil.getNowDate());
		fundsTrace.set("traceTime", DateUtil.getNowTime());
		fundsTrace.set("traceRemark", remark ); //"审核满标放款，确认用户投标支出"
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		return fundsTrace.save() ;
	}
	
	/**
	 * 更新理财人待还账户
	 * @param userCode
	 * @param beRecyCount			//待回收笔数
	 * @param beRecyPrincipal		//待收回本金
	 * @param beRecyInterest		//待收回利息
	 * @param reciedPrincipal		//已回收本金
	 * @param reciedInterest		//已回收利息
	 * @return
	 */
	public int updateBeRecyFunds(String userCode , int beRecyCount , long beRecyPrincipal , long beRecyInterest 
			,long reciedPrincipal , long reciedInterest ){
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String upSql = "update t_funds set beRecyCount=beRecyCount+?,beRecyPrincipal=beRecyPrincipal+?,"
				+ "beRecyInterest=beRecyInterest+?,reciedPrincipal=reciedPrincipal+?,reciedInterest=reciedInterest+?,"
				+ "updateDate=?,updateTime=?"
				+ " where userCode=?";
		
		return Db.update(upSql, beRecyCount , beRecyPrincipal , beRecyInterest , reciedPrincipal , reciedInterest ,
				nowDate , nowTime , userCode ) ;
	}
	
	/**
	 * 更新借款人待还信息
	 * @param userCode
	 * @param limit
	 * @param amount
	 * @param interest
	 * @param beRecyMFee
	 * @return
	 */
	public int updateBeRecyFunds4loan(String userCode , int limit , long amount ,
			long interest ,  int beRecyMFee ){
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String upSql = "update t_funds set loanTotal=loanTotal+?,loanCount=loanCount+?,"
				+ "beRecyPrincipal4loan=beRecyPrincipal4loan+?,beRecyInterest4loan=beRecyInterest4loan+?,"
				+ "beRecyMFee4loan=beRecyMFee4loan+?,updateDate=?,updateTime=?"
				+ " where userCode=?";
		return Db.update(upSql , amount , limit , amount ,interest ,beRecyMFee , nowDate ,
				nowTime , userCode );
	}
	
	/**
	 * 	流标资金账户操作，归还冻结余额
	 * @param userCode
	 * @param payAmount
	 * @return
	 */
	public Funds overLoan4funds(String userCode , long payAmount ){
		
		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;

		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", traceType.Y.val() );//交易类型
		fundsTrace.set("traceTypeName", traceType.Y.desc());//交易类型
		fundsTrace.set("fundsType",fundsType.J.val() );//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", funds.getLong("avBalance")+payAmount );//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance")-payAmount );//实时冻结余额冻结余额
		fundsTrace.set("traceFee", 0 );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", "流标，归还投资本金。" );
		fundsTrace.set("traceMac", "");
		
		if( fundsTrace.save() ){
			funds = frozeBalance2avBalance(userCode, payAmount) ;
		}
		
		return funds ;
	}
	
//	/**
//	 * 	流标资金账户操作，归还冻结余额
//	 * @param userCode
//	 * @param payAmount
//	 * @return
//	 */
//	public Funds revokeLoan4Funds(String userCode , long payAmount ){
//
//		FundsTrace fundsTrace = new FundsTrace();
//		Funds funds = getFundsByUserCode(userCode) ;
//
//		fundsTrace.set("traceCode", UIDUtil.generate());
//		fundsTrace.set("userCode", userCode );
//		fundsTrace.set("userName", funds.getStr("userName"));
//		fundsTrace.set("traceType", traceType.Y.val() );//交易类型
//		fundsTrace.set("traceTypeName", traceType.Y.desc());//交易类型
//		fundsTrace.set("fundsType",fundsType.J.val() );//资金类型
//		fundsTrace.set("traceAmount", payAmount );	//pay
//		fundsTrace.set("traceBalance", funds.getLong("avBalance")+payAmount );//实时余额,可用余额
//		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance")-payAmount );//实时冻结余额冻结余额
//		fundsTrace.set("traceFee", 0 );
//		fundsTrace.set("traceDate",DateUtil.getNowDate());
//		fundsTrace.set("traceTime",DateUtil.getNowTime());
//		fundsTrace.set("traceSynState",traceSynState.N.val());
//		fundsTrace.set("traceRemark", "借款标撤回" );
//		fundsTrace.set("traceMac", "");
//		
//		if( fundsTrace.save() ){
//			funds = frozeBalance2avBalance(userCode, payAmount) ;
//		}
//		
//		return funds ;
//	}
	
	/**
	 * 	操作可用余额交易，为封装业务提供便捷
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额
	 * @param traceFee	交易手续费
	 * @param tt		流水类型，枚举		
	 * @param ft		资金类型，枚举
	 * @param remark	交易备注
	 * @return
	 */
	public Funds doAvBalance4biz(String userCode , long payAmount , long traceFee ,  traceType tt , fundsType ft ,  String remark ){
		
		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;
		long avBalance = funds.getLong("avBalance") ;
		int doType = 1 ;//默认减少
		if( ft.val().equals( fundsType.J.val() ) ){
			//收入 +
			doType = 0 ;
			avBalance = avBalance + payAmount ;
		}else{
			//支出
			doType = 1 ;
			avBalance = avBalance - payAmount ;
		}
		
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", tt.val() );//交易类型
		fundsTrace.set("traceTypeName", tt.desc());//交易类型
		fundsTrace.set("fundsType",ft.val());//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", avBalance - traceFee );//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance") );//实时冻结余额冻结余额
		fundsTrace.set("traceFee", traceFee );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", remark );
		fundsTrace.set("traceMac", "");
		
		if( fundsTrace.save() )
			try {
				//更新非黑名单用户资金
//				String sql = "SELECT payUserCode from t_loan_trace where loanCode in(SELECT loanCode from t_settlement_early where earlyDate BETWEEN 20180521 and 20180528 and estatus != 'D') and not isnull(authCode) GROUP BY payUserCode";
//				List<String> lstBlackUser = Db.query(sql);
//				if (!lstBlackUser.contains(userCode)) {
					funds = doAvBalance(userCode, doType , payAmount-traceFee ) ;
//				}
			} catch (BaseBizRunTimeException e) {
				funds = null;
				throw e;
			}
			
		return funds ;
	}
	
	/**
	 * 	操作冻结余额交易，为封装业务提供便捷(还款专用,还款金额先划至冻结余额) WJW
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额
	 * @param traceFee	交易手续费
	 * @param tt		流水类型，枚举		
	 * @param ft		资金类型，枚举
	 * @param remark	交易备注
	 * @return
	 */
	public Funds doAvBalanceRepayment(String userCode , long payAmount , long traceFee ,  traceType tt , fundsType ft ,  String remark ){
		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;
		long frozeBalance = funds.getLong("frozeBalance") ;
		frozeBalance = frozeBalance + payAmount - traceFee ;
		
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", tt.val() );//交易类型
		fundsTrace.set("traceTypeName", tt.desc());//交易类型
		fundsTrace.set("fundsType",ft.val());//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", funds.getLong("avBalance"));//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//实时冻结余额
		fundsTrace.set("traceFee", traceFee );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", remark );
		fundsTrace.set("traceMac", "");
		
		if( fundsTrace.save() )
			try {
				funds = doFrozeBalance(userCode, 0, payAmount-traceFee);//本地添加冻结金额
			} catch (BaseBizRunTimeException e) {
				funds = null;
				throw e;
			}
			
		return funds ;
	}
	
	/**
	 * 	解冻冻结余额,记录流水 WJW
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额
	 * @param traceFee	交易手续费
	 * @param tt		流水类型，枚举		
	 * @param ft		资金类型，枚举
	 * @param remark	交易备注
	 * @return
	 */
	public Funds frozeBalance(String userCode , long payAmount , long traceFee ,  traceType tt , fundsType ft ,  String remark ){
		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;
		long frozeBalance = funds.getLong("frozeBalance") ;
		frozeBalance = frozeBalance - (payAmount-traceFee) ;
		
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", tt.val() );//交易类型
		fundsTrace.set("traceTypeName", tt.desc());//交易类型
		fundsTrace.set("fundsType",ft.val());//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", funds.getLong("avBalance")+(payAmount-traceFee));//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//实时冻结余额冻结余额
		fundsTrace.set("traceFee", traceFee );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", remark );
		fundsTrace.set("traceMac", "");
		
		if( fundsTrace.save() )
			try {
				funds = frozeBalance2avBalance(userCode, payAmount-traceFee);
			} catch (BaseBizRunTimeException e) {
				funds = null;
				throw e;
			}
			
		return funds ;
	}
	
	/**
	 * 	操作可用余额交易，以江西存管资金为准 WJW
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额
	 * @param traceFee	交易手续费
	 * @param tt		流水类型，枚举		
	 * @param ft		资金类型，枚举
	 * @param remark	交易备注
	 * @return
	 */
	public Funds doAvBalanceByJX(String userCode ,String jxAccountId, long payAmount , long traceFee ,  traceType tt , fundsType ft ,  String remark ){
		
		FundsTrace fundsTrace = new FundsTrace();
		Funds funds = getFundsByUserCode(userCode) ;
		
		Map<String, String> accountBalance = JXQueryController.balanceQuery(jxAccountId);
		Map<String, String> freezeBalance = JXQueryController.freezeAmtQuery(jxAccountId);
		long avBalance = StringUtil.getMoneyCent(accountBalance.get("availBal"));
		long frozeBalance = StringUtil.getMoneyCent(freezeBalance.get("bidAmt")) + StringUtil.getMoneyCent(freezeBalance.get("repayAmt")) + StringUtil.getMoneyCent(freezeBalance.get("plAmt"));
		
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode );
		fundsTrace.set("userName", funds.getStr("userName"));
		fundsTrace.set("traceType", tt.val() );//交易类型
		fundsTrace.set("traceTypeName", tt.desc());//交易类型
		fundsTrace.set("fundsType",ft.val());//资金类型
		fundsTrace.set("traceAmount", payAmount );	//pay
		fundsTrace.set("traceBalance", avBalance);//实时余额,可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//实时冻结余额冻结余额
		fundsTrace.set("traceFee", traceFee );
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark", remark );
		fundsTrace.set("traceMac", "");
		if(fundsTrace.save()){
			funds = syncAccount(userCode, avBalance, frozeBalance);
		}
			
		return funds ;
	}
	
	
	
	/**
	 * 	充值
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额，单位分
	 * @param remark	交易备注
	 * @return
	 */
	public Funds recharge(String userCode , long payAmount,long traceFee , String remark,String traceTypeValue ){
		Db.update("update t_funds set totalRecharge = totalRecharge + ? where userCode = ?",payAmount,userCode);
		if(traceTypeValue.equals("C")){
			return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.C, fundsType.J , remark ) ;
		}else if(traceTypeValue.equals("D")){
			return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.D, fundsType.J , remark ) ;
		}
		return null;
	}
	
	/**
	 * 	推荐有奖充值操作
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额，单位分
	 * @param remark	交易备注
	 * @return
	 */
	public Funds rechargeShare(String userCode , long payAmount,long traceFee , String remark ){
		Db.update("update t_funds set totalRecharge = totalRecharge + ? where userCode = ?",payAmount,userCode);
		return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.W , fundsType.J , remark ) ;
	}
	
	/**
	 * 	增加投标体验金
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额，单位分
	 * @param remark	交易备注
	 * @return
	 */
	public Funds rechargeTiYanJin1(String userCode , long payAmount,long traceFee , String remark ){
		Db.update("update t_funds set totalRecharge = totalRecharge + ? where userCode = ?",payAmount,userCode);
		return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.V , fundsType.J , remark ) ;
	}
	
	
	/**
	 * 好友投资返现
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额，单位分
	 * @param remark	交易备注
	 * @return
	 */
	public Funds rechargeTZFX_HY(String userCode , long payAmount,long traceFee , String remark ){
		Db.update("update t_funds set totalRecharge = totalRecharge + ? where userCode = ?",payAmount,userCode);
		return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.Q , fundsType.J , remark ) ;
	}
	
	/**
	 * 	好友投资返佣
	 * 
	 * @param userCode	用户编号
	 * @param payAmount	交易金额，单位分
	 * @param remark	交易备注
	 * @return
	 */
	public Funds rechargeTZFY_HY(String userCode , long payAmount,long traceFee , String remark ){
		Db.update("update t_funds set totalRecharge = totalRecharge + ? where userCode = ?",payAmount,userCode);
		return doAvBalance4biz(userCode, payAmount, traceFee ,traceType.O , fundsType.J , remark ) ;
	}
	
	/**
	 * 	获取用户资金账户情况
	 * @param userCode	用户编号
	 * @return
	 */
	public Funds getFundsByUserCode(String userCode){
		Funds funds = Funds.fundsDao.findById(userCode) ;
		return funds ;
	}
	
	/**
	 * 	获取可用余额
	 * @param userCode
	 * @return
	 */
	public long findAvBalanceById(String userCode){
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
		return funds.getLong("avBalance");
	}
	
	/**
	 * 	获取冻结余额
	 * @param userCode
	 * @return
	 */
	public long findFrozeBalanceById(String userCode){
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "frozeBalance");
		return funds.getLong("frozeBalance");
	}
	
	/**
	 * 预投标
	 * @param userCode
	 * @param amount
	 * @param traceFee
	 * @param traceRemark
	 * @return
	 */
	public boolean update4prepareBid(String userCode,long amount,
			long traceFee,String traceRemark){
		Funds funds = doAvBalance4biz(userCode, amount, traceFee, SysEnum.traceType.X,SysEnum.fundsType.D , traceRemark);
		if(funds!=null){
			return true;
		}
		return false;
	}
	
	/**
	 * 可用余额--冻结余额 转换
	 * @param amount		交易金额
	 * @param userCode		用户编码
	 * @param type			1 可用余额划入冻结余额    0 冻结余额划入可用余额
	 * @return
	 */
	public boolean convertBalance(long amount, String userCode, Integer type,Integer traceFee,String traceRemark){
		String traceType = "";
		String traceTypeName = "";
		String fundsType = "";
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "userCode,avBalance,frozeBalance");
		long avBalance = funds.getLong("avBalance");
		long frozeBalance = funds.getLong("frozeBalance");
		if(type==1){
			avBalance2froze(userCode, amount);
			traceType = SysEnum.traceType.F.val();
			traceTypeName = SysEnum.traceType.F.desc();
			fundsType = SysEnum.fundsType.D.val();
			avBalance = avBalance - amount;
			frozeBalance = frozeBalance + amount;
		}else if(type==0){
			frozeBalance2avBalance(userCode,amount);
			traceType = SysEnum.traceType.Y.val();
			traceTypeName = SysEnum.traceType.Y.desc();
			fundsType = SysEnum.fundsType.J.val();
			avBalance = avBalance + amount;
			frozeBalance = frozeBalance - amount;
		}
		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName",User.userDao.findByIdLoadColumns(userCode, "userName").get("userName",""));
		fundsTrace.set("traceType", traceType);
		fundsTrace.set("traceTypeName", traceTypeName);
		fundsTrace.set("fundsType", fundsType);
		fundsTrace.set("traceAmount", amount);
		fundsTrace.set("traceBalance",avBalance);
		fundsTrace.set("traceFrozeBalance", frozeBalance);
		fundsTrace.set("traceFee", traceFee);
		fundsTrace.set("traceRemark", traceRemark);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceSynState", SysEnum.traceSynState.N.val());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		
		if(fundsTrace.save()){
			return true;
		}
		return false;
	}
	
	/**
	 * 分页查询理财人资金账户,返回Map,可用于生成excel
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param userCode
	 * @param allKey
	 * @param accountType
	 * @return
	 */
	public Map<String,Object> findByPage4Noob1(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String allKey){
		String sqlSelect = "select t1.*,t3.userCardName ";
		String sqlFrom = " from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";
		String sqlOrder = "order by t1.updateDateTime desc,t1.uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		makeExp(buff, ps, "t1.updateDateTime", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.updateDateTime", "<=" , endDate , "and");
		makeExp(buff, ps, "t1.loanTotal", "<=" , 0+"" , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allKey, "and","or");
		
		if(!StringUtil.isBlank(allKey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allKey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t2.userMobile", "=" , x , "or");
		}
		
		Page<Funds> pages = Funds.fundsDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}
	
	/**
	 * 分页查询理财人资金账户对应的统计数据,返回Map,可用于生成excel
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param userCode
	 * @param allKey
	 * @param accountType
	 * @return
	 */
	public Map<String,Object> findByPage4Noob1WithSum(String beginDate, String endDate, String allKey){
		String sqlFrom = " from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		makeExp(buff, ps, "t1.updateDateTime", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.updateDateTime", "<=" , endDate , "and");
		makeExp(buff, ps, "t1.loanTotal", "<=" , 0+"" , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allKey, "and","or");
		
		if(!StringUtil.isBlank(allKey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allKey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t2.userMobile", "=" , x , "or");
		}
		
		Map<String,Object> result = new HashMap<String, Object>();
		long count_avBalance = Db.queryBigDecimal("select COALESCE(sum(avBalance),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_points = Db.queryBigDecimal("select COALESCE(sum(points),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_frozeBalance = Db.queryBigDecimal("select COALESCE(sum(frozeBalance),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_beRecyCount = Db.queryBigDecimal("select COALESCE(sum(beRecyCount),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_beRecyPrincipal = Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_beRecyInterest = Db.queryBigDecimal("select COALESCE(sum(beRecyInterest),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_reciedPrincipal = Db.queryBigDecimal("select COALESCE(sum(reciedPrincipal),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_reciedInterest = Db.queryBigDecimal("select COALESCE(sum(reciedInterest),0) "+ sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		result.put("count_avBalance", count_avBalance);result.put("count_points", count_points);result.put("count_frozeBalance", count_frozeBalance);
		result.put("count_beRecyCount", count_beRecyCount);result.put("count_beRecyPrincipal", count_beRecyPrincipal);result.put("count_beRecyInterest", count_beRecyInterest);
		result.put("count_reciedPrincipal", count_reciedPrincipal);result.put("count_reciedInterest", count_reciedInterest);
		return result;
	}
	
	/**
	 * 根据用户总投资本金排序查询
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Map<String,Object> queryFundsOrderByMoney(Integer pageNumber, Integer pageSize){
		String sqlSelect = "select payUserCode as userCode,payUserName as userName,sum(payAmount) as tzje ";
		String sqlFrom = " from t_loan_trace where loanState='N' or loanState='O' or loanState='P' or loanState='Q' GROUP BY payUserCode ";
		String sqlOrder = " order by sum(payAmount) desc ";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		Page<LoanTrace> pages = LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}

	/**
	 * 查询风险备用金
	 * @return
	 */
	public long queryRiskTotal(){
		return Db.queryLong("select riskTotal from t_sys_funds");
	}
	
	/**
	 * 分页查询借款人资金账户,返回Map,可用于生成excel
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param userCode
	 * @param allKey
	 * @param accountType
	 * @return
	 */
	public Map<String,Object> findByPage4Noob2(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String allKey){
		String sqlSelect = "select t1.*,t3.userCardName ";
		String sqlFrom = " from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";
		String sqlOrder = "order by t1.updateDateTime desc,t1.uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		makeExp(buff, ps, "t1.updateDateTime", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.updateDateTime", "<=" , endDate , "and");
		makeExp(buff, ps, "t1.loanTotal", ">" , 0+"" , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allKey, "and","or");
		
		if(!StringUtil.isBlank(allKey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allKey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t2.userMobile", "=" , x , "or");
		}
		
		Page<Funds> pages = Funds.fundsDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}
	
	/**
	 * 分页查询借款人资金账户对应的统计信息,返回Map,可用于生成excel
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param userCode
	 * @param allKey
	 * @param accountType
	 * @return
	 */
	public Map<String,Object> findByPage4Noob2WithSum(String beginDate, String endDate, String allKey){
		String sqlFrom = " from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		makeExp(buff, ps, "t1.updateDateTime", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.updateDateTime", "<=" , endDate , "and");
		makeExp(buff, ps, "t1.loanTotal", ">" , 0+"" , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allKey, "and","or");
		
		if(!StringUtil.isBlank(allKey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allKey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t2.userMobile", "=" , x , "or");
		}
		
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		long count_loanTotal = Db.queryBigDecimal("select COALESCE(sum(loanTotal),0)"+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_loanCount = Db.queryBigDecimal("select COALESCE(sum(loanCount),0)"+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_loanSuccessCount = Db.queryBigDecimal("select COALESCE(sum(loanSuccessCount),0) + COALESCE(sum(loanBySysCount),0)"+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_beRecyPrincipal4loan = Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal4loan),0)"+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		long count_beRecyInterest4loan = Db.queryBigDecimal("select COALESCE(sum(beRecyInterest4loan),0)"+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		result.put("count_loanTotal", count_loanTotal);result.put("count_loanCount", count_loanCount);
		result.put("count_loanSuccessCount", count_loanSuccessCount);result.put("count_beRecyPrincipal4loan", count_beRecyPrincipal4loan);
		result.put("count_beRecyInterest4loan", count_beRecyInterest4loan);
		return result;
	}
	
	/**
	 * 分页查询会员资金
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @param beginDate		开始日期(更新日期)
	 * @param endDate		结束日期(更新日期)
	 * @return
	 */
	public Page<Funds> findByPage(Integer pageNumber, Integer pageSize, String beginDate, String endDate,String userCode, String allKey){
		
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode left join t_user_info t3 on t1.userCode = t3.userCode ";
		String sqlOrder = "order by t1.updateDateTime desc,t1.uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		makeExp(buff, ps, "t1.userCode", "=" , userCode , "and");
		
		makeExp(buff, ps, "t1.updateDateTime", ">=" , beginDate , "and");
		makeExp(buff, ps, "t1.updateDateTime", "<=" , endDate , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, ps, keys, allKey, "and","or");
		
		if(!StringUtil.isBlank(allKey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allKey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "t2.userMobile", "=" , x , "or");
		}

		return Funds.fundsDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;

	}
	
	/**
	 * 查询借款人的资金情况
	 * @param pageNumber
	 * @param pageSize	
	 * @return
	 */
	public Page<Funds> findLoanUserFundsByPage(Integer pageNumber, Integer pageSize,String allkey){
		
		String sqlSelect = "select t1.userName,t1.userCode,t1.beRecyPrincipal4loan,t1.beRecyInterest4loan,t1.loanCount,t1.loanSuccessCount,t1.loanTotal,t3.userCardName trueName";
		String sqlFrom = " from t_funds t1 inner join t_user t2 inner join t_user_info t3 on t1.userCode = t3.userCode and t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.updateDateTime desc";
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.loanTotal", ">" , 0+"" , "and");
		
		String[] keys = new String[]{"t1.userName","t2.userEmail","t3.userCardName"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		return Funds.fundsDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray()); 
	}
	
	/**
	 * 查询统计新增用户数量
	 * @param date	日期
	 * @param type	类型 0 理财人  1借款人
	 * @return
	 */
	public long countFundsByDate(String date,int type){
		if(type==0)
			return Db.queryLong("select count(t1.userCode) from t_user t1 inner join t_funds t2 on t1.userCode = t2.userCode where t1.regDate = ? and t2.loanTotal<=0",date);
		else if(type==1)
			return Db.queryLong("select count(t1.userCode) from t_user t1 inner join t_funds t2 on t1.userCode = t2.userCode where t1.regDate = ? and t2.loanTotal>0",date);
		return 0;
	}
	/**
	 * 查询统计续借用户数量
	 * @param date 日期
	 * @return
	 */
	public long countFundsByXuJie(String date){
		return Db.queryLong("select count(distinct t1.userCode) from t_funds t1 inner join t_loan_info t2 on t1.userCode = t2.userCode inner join t_user t3 on t1.userCode = t3.userCode where (t1.loanCount+t1.loanSuccessCount+t1.loanBySysCount) > t2.loanTimeLimit and t3.regDate=?",date);
	}
	
	/**
	 * 查询用户投资排名，总的！
	 * @param userCode
	 * @return
	 */
	public long countMyRank(String userCode){
		long myrank = -1;
		if(StringUtil.isBlank(userCode))
			return -1;
		try {
			long mymoney = Db.queryLong("select beRecyPrincipal+reciedPrincipal from t_funds where userCode = ?",userCode);
			myrank = Db.queryLong("select count(userCode) from t_funds where (beRecyPrincipal+reciedPrincipal) > ?",mymoney);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
		return myrank+1;
	}
	/**
	 * 根据时间统计查询还款-本金
	 * @param date
	 * @return
	 */
	public long sumAdmountByDate(String date){
		return Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceDate = ? and traceType = ?",date,SysEnum.traceType.U.val()).longValue();
	}
	
	/**
	 * 根据时间区间统计查询还款-本金
	 * @param date
	 * @return
	 */
	public long sumAdmountByDate(String beginDate, String endDate){
		return Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceDate >= ? and traceDate <= ? and traceType = ?",beginDate, endDate,SysEnum.traceType.U.val()).longValue();
	}
	
	/**
	 * 根据时间统计查询还款-利息
	 * @param date
	 * @return
	 */
	public long sumInterestByDate(String date){
		return Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceDate = ? and traceType = ?",date,SysEnum.traceType.I.val()).longValue();
	}
	
	/**
	 * 根据时间区间统计查询还款-利息
	 * @param date
	 * @return
	 */
	public long sumInterestByDate(String beginDate, String endDate){
		return Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceDate >= ? and traceDate <= ? and traceType = ?",beginDate, endDate,SysEnum.traceType.I.val()).longValue();
	}
	
	/**
	 * 增加风险备用金
	 * @param riskFee
	 */
	public void updateRiskTotal(int riskFee){
		Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1",
				riskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
	}

	/**
	 * 查询积分榜前五位
	 * @param
	 * @return 
	 */
	public Page<Funds> queryPointTop5() {
		String sqlSelect = "select userName,points";
		String sqlFrom = "from t_funds";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		String sqlOrder = " order by points desc";
		return Funds.fundsDao.paginate(1, 5, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}
	
	/**
	 * 查询积分榜六到十位
	 * @param
	 * @return 
	 */
	public Page<Funds> queryPoint6_10() {
		String sqlSelect = "select userName,points";
		String sqlFrom = "from t_funds";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		String sqlOrder = " order by points desc";
		return Funds.fundsDao.paginate(2, 5, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}
	
	/**
	 *	申请提现,修改可用余额,保存资金流水
	 * @author WCF 20170627
	 * @param userCode		用户编码
	 * @param payAmount		交易金额
	 * @param traceFee		额外交易费用
	 * @return
	 */
	public boolean withdrawals4Fuiou(String userCode, Long payAmount, Integer traceFee, String traceRemark){
		Funds funds = getFundsByUserCode(userCode) ;
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");

		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",SysEnum.traceType.G.val());//交易类型
		fundsTrace.set("traceTypeName",SysEnum.traceType.G.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.D.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance-payAmount);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//冻结余额
		fundsTrace.set("traceFee",traceFee);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark);
		fundsTrace.set("traceMac", "");
		
		if(fundsTrace.save()){
			long overFreeAmount = 0l;	// 提现超出免费额度的金额
			long totalWithdraw = payAmount+funds.getLong("totalWithdraw");
			long allLoanAmount = funds.getLong("beRecyPrincipal")+funds.getLong("reciedPrincipal")+funds.getLong("reciedInterest");
			if(totalWithdraw > allLoanAmount){
				overFreeAmount = payAmount - (totalWithdraw - allLoanAmount);
			}
			int x = Db.update("update t_funds set avBalance = avBalance - ?,frozeBalance = frozeBalance - ?,updateDateTime = ?,updateDate = ?,updateTime = ?,totalWithdraw = totalWithdraw + ? where userCode=?",payAmount,0,DateUtil.getNowDateTime(),DateUtil.getNowDate(),DateUtil.getNowTime(),overFreeAmount,userCode);
			if( funds != null && x > 0){
				return true ;
			}
		}
		return false;
	}
	/**
	 * ws 20171122
	 * 扣除或增加 加息额度
	 * type = 0 增加   1  扣除
	 */
	public Funds deductRewardRateAmount(String userCode , long doAmount ,int type){
		Funds funds = getFundsByUserCode(userCode);
		long rewardRateAmount = funds.getLong("rewardRateAmount") ;
		if(type==1){
			if( rewardRateAmount < doAmount ){
				throw new BaseBizRunTimeException("B1", "账户加息额度不足!", rewardRateAmount ) ;
			}
		}
		String operation="";
		if(type==0){
			operation="+";
		}else if(type==1){
			operation="-";
		}
		String upSql = "update t_funds set rewardRateAmount=rewardRateAmount"+operation+"?,updateDateTime=?,"
				+ "updateDate=?,updateTime=?"
				+ " where userCode=? and (rewardRateAmount"+operation+"?)>=0";
		
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String nowDateTime = nowDate + nowTime ;
		
		int result = Db.update(upSql, doAmount , nowDateTime , nowDate , nowTime , userCode , doAmount ) ;
		if( result > 0 ){
			//更新成功
			funds = getFundsByUserCode(userCode);
		}else{
			//更新失败
			throw new BaseBizRunTimeException("B2", "账户扣除或增加加息额度异常，可用额度不足或其他原因!", funds.getLong("rewardRateAmount") ) ;
		}
		return funds ;
	}
	
	/**	WJW
	 * 更新用户昨日待收
	 * @param userCode	用户编码
	 * @param oldBeRecyAmount	用户昨日待收
	 * @return
	 */
	public boolean updateOldBeRecyAmount(String userCode,long oldBeRecyAmount){
		return Db.update("update t_funds set oldBeRecyAmount=? where userCode=?", oldBeRecyAmount,userCode)>0;
		
	}
	
	/**
	 * 提现冲正/失败——资金回退
	 * @param userCode
	 * @param payAmount	提现金额
	 * @param traceRemark 交易描述
	 * @return
	 */
	public boolean withdrawRevers(String userCode, Long payAmount, String traceRemark){
		Funds funds = getFundsByUserCode(userCode) ;
		Long avBalance = funds.getLong("avBalance");
		Long frozeBalance = funds.getLong("frozeBalance");

		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("userCode", userCode);
		
		User user = User.userDao.findByIdLoadColumns(userCode, "userName");
		fundsTrace.set("userName", user.get("userName"));
		fundsTrace.set("traceType",SysEnum.traceType.K.val());//交易类型
		fundsTrace.set("traceTypeName",SysEnum.traceType.K.desc());//交易类型
		fundsTrace.set("fundsType",SysEnum.fundsType.J.val());//资金类型收入
		fundsTrace.set("traceAmount",payAmount);
		fundsTrace.set("traceBalance", avBalance+payAmount);//可用余额
		fundsTrace.set("traceFrozeBalance", frozeBalance);//冻结余额
		fundsTrace.set("traceFee",0);
		fundsTrace.set("traceDate",DateUtil.getNowDate());
		fundsTrace.set("traceTime",DateUtil.getNowTime());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceSynState",traceSynState.N.val());
		fundsTrace.set("traceRemark",traceRemark);
		fundsTrace.set("traceMac", "");
		
		if(fundsTrace.save()){
			int x = Db.update("update t_funds set avBalance = avBalance + ?,updateDateTime = ?,updateDate = ?,updateTime = ?,totalWithdraw = totalWithdraw - ? where userCode=?",payAmount,DateUtil.getNowDateTime(),DateUtil.getNowDate(),DateUtil.getNowTime(),payAmount,userCode);
			if( funds != null && x > 0){
				return true ;
			}
		}
		return false;
	}
	
	/**
	 * 时间段内用户指定资金类型流水总金额
	 * */
	public long sumTotalBackAmount(String userCode,String traceType,String beginDate,String endDate){
		return Db.queryBigDecimal("SELECT COALESCE(SUM(traceAmount),0) from t_funds_trace where traceType = ? and userCode = ? and traceDate BETWEEN ? and ?",traceType,userCode,beginDate,endDate).longValue();
	}
	
	/**
	 * 根据投标流水查询用户待收
	 * @param userCode	用户编号
	 * @param productType	产品类型
	 * @param traceState	流水状态
	 * @return
	 */
	public long[] sumLeftAmount4Type(String userCode,String productType,String traceState){
		long[] result = new long[2];
		Object[] record = Db.queryFirst("SELECT COALESCE(sum(leftAmount),0), COALESCE(sum(leftInterest),0) FROM t_loan_trace WHERE productType = ? AND traceState = ? AND payUserCode = ? ",productType,traceState,userCode);
		result[0] = Long.valueOf(record[0] + "");
		result[1] = Long.valueOf(record[1] + "");
		return result;
	}
}