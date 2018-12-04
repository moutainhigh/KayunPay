package com.dutiantech.service;

import java.util.List;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.AutoMap;
import com.dutiantech.model.Funds;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class AutoMapSerivce extends BaseService{
	
	static Logger _LOG = Logger.getLogger( AutoMapSerivce.class.getName() );
	static{
		LoggerUtil.initLogger("autoMap", _LOG );
	}
	
	
	public long genAutoMap( List<AutoLoan_v2> autos ){
		int count = 0 ;
		for(AutoLoan_v2 auto : autos ){
			try{
				count ++ ;
				genAutoMap(auto , count );
			}catch(Exception e){
				_LOG.info("初始化至：" + count + " 出错,"  + JSONObject.toJSONString( auto ));
				e.printStackTrace(); 
			}
		}
		
		return count ;
	}
	
	public int genAutoMap(AutoLoan_v2 autoLoan){
		
		int rank = Db.queryInt("select max(rankValue) from t_auto_map ");//where autoState='A'
		
		return genAutoMap(autoLoan, rank) ;
	}
	
	/**
	 * 	生成索引
	 * @param autoLoan
	 * @param rank
	 * @return
	 */
	public int genAutoMap( AutoLoan_v2 autoLoan , int rank ){
		
		Db.update("delete from t_auto_map where userCode=?", autoLoan.getStr("userCode") ) ;
		
		AutoMap map = new AutoMap() ;
		map.set("userCode", autoLoan.getStr("userCode") ) ;
		map.set("userName", autoLoan.getStr("userName") ) ;
		map.set("autoAmount", 0 ) ;
		map.set("onceMinAmount", autoLoan.getLong("onceMinAmount") ) ;
		map.set("onceMaxAmount", autoLoan.getLong("onceMaxAmount") ) ;
		map.set("refundType", autoLoan.getStr("refundType") ) ;
		map.set("userCode", autoLoan.getStr("userCode") ) ;
		map.set("autoState", autoLoan.getStr("autoState") ) ;
		map.set("rankValue", rank ) ;

		int minLimit = autoLoan.getInt("autoMinLim");
		int maxLimit = autoLoan.getInt("autoMaxLim"); 
		for(;minLimit <= maxLimit ; minLimit++ ) {
			map.set("onceLimit", minLimit ) ;
			map.remove("aid");
			map.save() ;
//			_LOG.info("Save ok!" + rank );
		}
		
		return maxLimit-minLimit + 1 ;
		
	}
	
	/**
	 * 查询用户自动投标的综合排名索引
	 * @param userCode	用户编码
	 * @return
	 */
	public int queryRankVal(String userCode){
		long tmp = Db.queryLong("select count(*) from t_auto_map where autoState = 'A'");
		if(tmp <1 ){
			return 0;
		}
		return Db.queryInt("select rankValue from t_auto_map where userCode = ? and autoState = 'A' limit 0,1",userCode);
	}
	
	/**
	 * 查询最后一位rankVal排名索引
	 * @return
	 */
	public int queryLastRankVal(){
		long tmp = Db.queryLong("select count(*) from t_auto_map where autoState = 'A'");
		if(tmp <1 ){
			return 0;
		}
		return Db.queryInt("select rankValue from t_auto_map where autoState = 'A' order by rankValue desc limit 0,1");
	}
	
	/**
	 * 查询最后一位rankVal排名索引
	 * @return
	 */
	public int queryFirstRankVal(){
		long tmp = Db.queryLong("select count(*) from t_auto_map where autoState = 'A'");
		if(tmp <1 ){
			return 0;
		}
		return Db.queryInt("select rankValue from t_auto_map where autoState = 'A' order by rankValue asc limit 0,1");
	}
	
	/**
	 * 强制重置用户自动投标排名，排到末尾
	 * @param autoLoan_v2
	 */
	public void resetRankVal_ADV(AutoLoan_v2 autoLoan_v2){
		Db.update("delete from t_auto_map where userCode = ?",autoLoan_v2.getStr("userCode"));
		
		AutoMap map = new AutoMap() ;
		map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
		map.set("userName", autoLoan_v2.getStr("userName") ) ;
		map.set("autoAmount", 0 ) ;
		map.set("onceMinAmount", autoLoan_v2.getLong("onceMinAmount") ) ;
		map.set("onceMaxAmount", autoLoan_v2.getLong("onceMaxAmount") ) ;
		map.set("refundType", autoLoan_v2.getStr("refundType") ) ;
		map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
		map.set("autoState", autoLoan_v2.getStr("autoState") ) ;
		map.set("rankValue", queryLastRankVal() + 1) ;

		int minLimit = autoLoan_v2.getInt("autoMinLim");
		int maxLimit = autoLoan_v2.getInt("autoMaxLim"); 
		for(;minLimit <= maxLimit ; minLimit++ ) {
			map.set("onceLimit", minLimit ) ;
			map.remove("aid");
			map.save() ;
		}
	}
	
	/**
	 * 正常重置用户自动投标排名，比如自动投标已投标之后重置排名
	 * @param autoLoan_v2
	 */
	public void resetRankVal_NORMAL(AutoLoan_v2 autoLoan_v2){
		int xx = Db.update("delete from t_auto_map where userCode = ? and autoState = 'B'",autoLoan_v2.getStr("userCode"));
		if(xx > 0){
			AutoMap map = new AutoMap() ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("userName", autoLoan_v2.getStr("userName") ) ;
			map.set("autoAmount", 0 ) ;
			map.set("onceMinAmount", autoLoan_v2.getLong("onceMinAmount") ) ;
			map.set("onceMaxAmount", autoLoan_v2.getLong("onceMaxAmount") ) ;
			map.set("refundType", autoLoan_v2.getStr("refundType") ) ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("autoState", autoLoan_v2.getStr("autoState") ) ;
			map.set("rankValue", queryLastRankVal() + 1) ;

			int minLimit = autoLoan_v2.getInt("autoMinLim");
			int maxLimit = autoLoan_v2.getInt("autoMaxLim"); 
			for(;minLimit <= maxLimit ; minLimit++ ) {
				map.set("onceLimit", minLimit ) ;
				map.remove("aid");
				map.save() ;
			}
		}
	}
	
	/**
	 * 根据用户编码查询一个用户的排名号
	 * @param userCode
	 * @return
	 */
	public int queryRankNum(String userCode){
		int userRankVal = Db.queryInt("select rankValue from t_auto_map where userCode = ? and autoState = 'A' limit 0,1",userCode);
		return Db.queryLong("select COUNT(DISTINCT userCode) from t_auto_map where rankValue <= ? and autoState='A'",userRankVal).intValue();
	}
	
	/**
	 * 根据排名索引查询一个用户的排名号
	 * @param rankVal 用户的排名索引
	 * @return
	 */
	public int queryRankNum(int rankVal){
		return Db.queryLong("select COUNT(DISTINCT userCode) from t_auto_map where rankValue <= ? and autoState='A'",rankVal).intValue();
	}
	
	/**
	 * 查询用户具体借款期限条件下自动投标的排位
	 * @param rankVal	用户综合排名索引
	 * @return
	 */
	public List<Record> queryRankDetail(int rankVal,int startLimit, int endLimit){
		return Db.find("select m.onceLimit as loanLimit,sum(f.avBalance) as amount,count(m.onceLimit) as quantity from t_auto_map m,t_funds f where m.userCode=f.userCode and m.rankValue<=? and m.autoState='A' and m.onceLimit >= ? and m.onceLimit <= ? group by m.onceLimit order by onceLimit",rankVal,startLimit,endLimit);
	}
	
	
	
	/**
	 * 验证自动投标状态
	 * @param userCode
	 * @return 0 还没有自动投标配置, -1 异常。  1 排队中  2停用  3已排到
	 */
	public int validateAutoState(String userCode){
		String autoState = "XXX";
		try {
			long xxx = Db.queryLong("select count(aid) from t_auto_loan_v2 where userCode = ?",userCode);
			if(xxx < 1){
				return 0;
			}
			autoState = Db.queryStr("select autoState from t_auto_loan_v2 where userCode = ? order by aid desc limit 0,1",userCode);
			if(StringUtil.isBlank(autoState)){
				return -1;
			}
		} catch (Exception e) {
			autoState = "XXX";
		}
		if(autoState.equals("A")){
			return 1;
		}else if(autoState.equals("C")){
			return 2;
		}else if(autoState.equals("B")){
			return 3;
		}
		return -1;
	}
	
	/**
	 * 清除自动投标设置
	 * @param userCode
	 */
	public void clearSettings(String userCode){
		Db.update("delete from t_auto_loan_v2 where userCode = ?",userCode);
		Db.update("delete from t_auto_map where userCode = ?",userCode);
	}
	
	
	/**
	 * 自动投标状态从排队中改为已排到
	 * @param userCode
	 */
	public boolean changeAutoState2B(String userCode){
		Db.update("update t_auto_loan_v2 set autoState = 'B' where autoState = 'A' and userCode = ?",userCode);
		return Db.update("update t_auto_map set autoState = 'B' where autoState = 'A' and userCode = ?",userCode) > 0;
	}
	
	/**
	 * 自动投标状态从排队中改为暂停
	 * @param userCode
	 */
	public boolean changeAutoState2C(String userCode){
		Db.update("update t_auto_loan_v2 set autoState = 'C' where autoState = 'A' and userCode = ?",userCode);
		return Db.update("update t_auto_map set autoState = 'C' where autoState = 'A' and userCode = ?",userCode) > 0;
	}
	
	/**
	 * 自动投标状态从暂停改为排队中
	 * @param userCode
	 */
	public boolean changeAutoState2A(String userCode){
		Db.update("update t_auto_loan_v2 set autoState = 'A' where autoState = 'C' and userCode = ?",userCode);
		return Db.update("update t_auto_map set autoState = 'A' where autoState = 'C' and userCode = ?",userCode) > 0;
	}
	
	/**
	 * 保存投标配置
	 * @param userCode
	 * @param userName
	 * @param onceMinAmount
	 * @param onceMaxAmount
	 * @param autoMinLim
	 * @param autoMaxLim
	 * @param refundType
	 * @param productType
	 * @return
	 */
	public boolean saveAutoLoanSettings(String userCode,String userName,long onceMinAmount, long onceMaxAmount, int autoMinLim, int autoMaxLim, String refundType,String productType,String priorityMode,String useTicket,String autoType){
		
		int rollingState = validateAutoState(userCode);//排队中的才可更新，暂停的不更新 直接保存新的排名
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
		int rankSettings = 0;
		boolean addNewBee = false;
		boolean autoLoanSettings = false;
		AutoLoan_v2 autoLoan_v2 = null;
		if( rollingState == 1){//排队中
			//自动投标排队中，如果更新期限、还款方式、产品类型，则新增一个重新排名
			autoLoan_v2 = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode = ? and autoState = 'A'",userCode);
			
			if( autoLoan_v2.getInt("autoMinLim") != autoMinLim || autoLoan_v2.getInt("autoMaxLim") != autoMaxLim || !autoLoan_v2.getStr("refundType").equals(refundType)){
				addNewBee = true;
			}else{
				//保留排名并更新自动投标配置的话，不更新期限 还款方式 产品类型那些字段
				autoLoan_v2.set("userName", userName);
				autoLoan_v2.set("autoAmount", funds.getLong("avBalance"));
				autoLoan_v2.set("onceMinAmount",onceMinAmount);
				autoLoan_v2.set("onceMaxAmount", onceMaxAmount);
				autoLoan_v2.set("autoDateTime", DateUtil.getNowDateTime());
				autoLoan_v2.set("priorityMode", priorityMode);
				autoLoan_v2.set("useTicket", useTicket);
				autoLoan_v2.set("productType", productType);
				autoLoan_v2.set("autoType", autoType);
				Db.update("update t_auto_map set onceMinAmount = ?,onceMaxAmount = ? where userCode = ? and autoState = 'A'",onceMinAmount,onceMaxAmount,userCode);
				autoLoanSettings = autoLoan_v2.update();
			}
		}else if(rollingState == 2){//已停用
			addNewBee = true;
		}else{
			//没有排队中的自动投标，新增一个
			addNewBee = true;
		}
		if(addNewBee){
			Db.update("delete from t_auto_map where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
			Db.update("delete from t_auto_loan_v2 where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
			
			autoLoan_v2 = new AutoLoan_v2();
			autoLoan_v2.set("userCode", userCode);
			autoLoan_v2.set("userName", userName);
			autoLoan_v2.set("autoAmount", funds.getLong("avBalance"));
			autoLoan_v2.set("onceMinAmount",onceMinAmount);
			autoLoan_v2.set("onceMaxAmount", onceMaxAmount);
			autoLoan_v2.set("autoMinLim", autoMinLim);
			autoLoan_v2.set("autoMaxLim", autoMaxLim);
			autoLoan_v2.set("refundType", refundType);
			autoLoan_v2.set("autoState", "A");
			autoLoan_v2.set("payAmount", 0);
			autoLoan_v2.set("productType", productType);
			autoLoan_v2.set("autoDateTime", DateUtil.getNowDateTime());
			autoLoan_v2.set("priorityMode", priorityMode);
			autoLoan_v2.set("useTicket", useTicket);
			autoLoan_v2.set("autoType", autoType);
			autoLoanSettings = autoLoan_v2.save();
			
			AutoMap map = new AutoMap() ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("userName", autoLoan_v2.getStr("userName") ) ;
			map.set("autoAmount", 0 ) ;
			map.set("onceMinAmount", autoLoan_v2.getLong("onceMinAmount") ) ;
			map.set("onceMaxAmount", autoLoan_v2.getLong("onceMaxAmount") ) ;
			map.set("refundType", autoLoan_v2.getStr("refundType") ) ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("autoState", "A" ) ;
			map.set("rankValue", queryLastRankVal()+1 ) ;

			int minLimit = autoLoan_v2.getInt("autoMinLim");
			int maxLimit = autoLoan_v2.getInt("autoMaxLim"); 
			for(;minLimit <= maxLimit ; minLimit++ ) {
				map.set("onceLimit", minLimit ) ;
				map.remove("aid");
				if(map.save()){
					rankSettings ++;
				}
			}
		}
		if(autoLoanSettings){
			if(addNewBee && rankSettings >0){
				return true;
			}else if(addNewBee == false){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 保存投标配置
	 * @param userCode
	 * @param userName
	 * @param onceMinAmount
	 * @param onceMaxAmount
	 * @param autoMinLim
	 * @param autoMaxLim
	 * @param refundType
	 * @param productType
	 * @param orderId
	 * @param deadLine
	 * @return
	 */
	public boolean saveAutoLoanSettings(String userCode,String userName,long onceMinAmount, long onceMaxAmount, int autoMinLim, int autoMaxLim, String refundType,String productType,String priorityMode,String useTicket,String autoType,String orderId,String deadLine){
		
		int rollingState = validateAutoState(userCode);//排队中的才可更新，暂停的不更新 直接保存新的排名
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
		int rankSettings = 0;
		boolean addNewBee = false;
		boolean autoLoanSettings = false;
		AutoLoan_v2 autoLoan_v2 = null;
		if( rollingState == 1){//排队中
			//自动投标排队中，如果更新期限、还款方式、产品类型，则新增一个重新排名
			autoLoan_v2 = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode = ? and autoState = 'A'",userCode);
			
			if( autoLoan_v2.getInt("autoMinLim") != autoMinLim || autoLoan_v2.getInt("autoMaxLim") != autoMaxLim || !autoLoan_v2.getStr("refundType").equals(refundType)){
				addNewBee = true;
			}else{
				//保留排名并更新自动投标配置的话，不更新期限 还款方式 产品类型那些字段
				autoLoan_v2.set("userName", userName);
				autoLoan_v2.set("autoAmount", funds.getLong("avBalance"));
				autoLoan_v2.set("onceMinAmount",onceMinAmount);
				autoLoan_v2.set("onceMaxAmount", onceMaxAmount);
				autoLoan_v2.set("autoDateTime", DateUtil.getNowDateTime());
				autoLoan_v2.set("priorityMode", priorityMode);
				autoLoan_v2.set("useTicket", useTicket);
				autoLoan_v2.set("productType", productType);
				autoLoan_v2.set("autoType", autoType);
				autoLoan_v2.set("jxOrderId", orderId);
				autoLoan_v2.set("deadLine", deadLine);
				Db.update("update t_auto_map set onceMinAmount = ?,onceMaxAmount = ? where userCode = ? and autoState = 'A'",onceMinAmount,onceMaxAmount,userCode);
				autoLoanSettings = autoLoan_v2.update();
			}
		}else if(rollingState == 2){//已停用
			addNewBee = true;
		}else{
			//没有排队中的自动投标，新增一个
			addNewBee = true;
		}
		if(addNewBee){
			Db.update("delete from t_auto_map where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
			Db.update("delete from t_auto_loan_v2 where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
			
			autoLoan_v2 = new AutoLoan_v2();
			autoLoan_v2.set("userCode", userCode);
			autoLoan_v2.set("userName", userName);
			autoLoan_v2.set("autoAmount", funds.getLong("avBalance"));
			autoLoan_v2.set("onceMinAmount",onceMinAmount);
			autoLoan_v2.set("onceMaxAmount", onceMaxAmount);
			autoLoan_v2.set("autoMinLim", autoMinLim);
			autoLoan_v2.set("autoMaxLim", autoMaxLim);
			autoLoan_v2.set("refundType", refundType);
			autoLoan_v2.set("autoState", "A");
			autoLoan_v2.set("payAmount", 0);
			autoLoan_v2.set("productType", productType);
			autoLoan_v2.set("autoDateTime", DateUtil.getNowDateTime());
			autoLoan_v2.set("priorityMode", priorityMode);
			autoLoan_v2.set("useTicket", useTicket);
			autoLoan_v2.set("autoType", autoType);
			autoLoan_v2.set("jxOrderId", orderId);
			autoLoan_v2.set("deadLine", deadLine);
			autoLoanSettings = autoLoan_v2.save();
			
			AutoMap map = new AutoMap() ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("userName", autoLoan_v2.getStr("userName") ) ;
			map.set("autoAmount", 0 ) ;
			map.set("onceMinAmount", autoLoan_v2.getLong("onceMinAmount") ) ;
			map.set("onceMaxAmount", autoLoan_v2.getLong("onceMaxAmount") ) ;
			map.set("refundType", autoLoan_v2.getStr("refundType") ) ;
			map.set("userCode", autoLoan_v2.getStr("userCode") ) ;
			map.set("autoState", "A" ) ;
			map.set("rankValue", queryLastRankVal()+1 ) ;

			int minLimit = autoLoan_v2.getInt("autoMinLim");
			int maxLimit = autoLoan_v2.getInt("autoMaxLim"); 
			for(;minLimit <= maxLimit ; minLimit++ ) {
				map.set("onceLimit", minLimit ) ;
				map.remove("aid");
				if(map.save()){
					rankSettings ++;
				}
			}
		}
		if(autoLoanSettings){
			if(addNewBee && rankSettings >0){
				return true;
			}else if(addNewBee == false){
				return true;
			}else{
				return false;
			}
		}
		return false;
	}
	
	/**
	 * 查询开启或关闭状态下的自动投标配置
	 * @param userCode
	 * @param autoState
	 * @return
	 */
	public AutoLoan_v2 queryAutoLoanByUserCode(String userCode){
		return AutoLoan_v2.autoLoanDao.findFirst(
				"select * from t_auto_loan_v2 where userCode = ? and (autoState = 'A' or autoState = 'C')", userCode);
	}
	
	/**
	 * 创建新的自动投标流水   带江西存管自动签约设置
	 * @param userCode
	 * @param userName
	 * @param onceMinAmount
	 * @param onceMaxAmount
	 * @param autoMinLim
	 * @param autoMaxLim
	 * @param refundType
	 * @param productType
	 * @param priorityMode
	 * @param useTicket
	 * @param autoType
	 * @param jxOrderId
	 * @param deadLine
	 * @param tempData
	 * @return
	 */
	public boolean createNewAutoLoan(String userCode,String userName,long onceMinAmount, long onceMaxAmount, int autoMinLim, int autoMaxLim, String refundType,String productType,String priorityMode,String useTicket,String autoType,String orderId,String deadLine,String tempData,String autoState){
		Db.update("delete from t_auto_map where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
		Db.update("delete from t_auto_loan_v2 where userCode = ? and (autoState = 'A' or autoState = 'C')",userCode);
		AutoLoan_v2 autoLoan_v2 = new AutoLoan_v2();
		autoLoan_v2.set("userCode", userCode);
		autoLoan_v2.set("userName", userName);
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
		autoLoan_v2.set("autoAmount", funds.getLong("avBalance"));
		autoLoan_v2.set("onceMinAmount",onceMinAmount);
		autoLoan_v2.set("onceMaxAmount", onceMaxAmount);
		autoLoan_v2.set("autoMinLim", autoMinLim);
		autoLoan_v2.set("autoMaxLim", autoMaxLim);
		autoLoan_v2.set("refundType", refundType);
		autoLoan_v2.set("autoState", autoState);
		autoLoan_v2.set("payAmount", 0);
		autoLoan_v2.set("productType", productType);
		autoLoan_v2.set("autoDateTime", DateUtil.getNowDateTime());
		autoLoan_v2.set("priorityMode", priorityMode);
		autoLoan_v2.set("useTicket", useTicket);
		autoLoan_v2.set("autoType", autoType);
		autoLoan_v2.set("jxOrderId", orderId);
		autoLoan_v2.set("deadLine", deadLine);
		return autoLoan_v2.save();
	}
}
