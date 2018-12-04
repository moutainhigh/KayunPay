package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dutiantech.model.MarketUser;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class MarketUserService extends BaseService {
	
	private static final String basic_selectFields = " id,muCode,userCode,userName,userCardName,userMobile,userAddress,remark,mCode,mName,point,issue,addDateTime,updateDateTime";

	/**
	 * 查询兑换用户列表
	 * @param pageNumber
	 * @param pageSize
	 * @param issue
	 * @param userCode
	 * @return
	 */
	public Map<String,Object> queryMarketUser(Integer pageNumber, Integer pageSize,
			String issue ,String userCode,String mCode) {
		String sqlSelect = "select t1.id id,t1.muCode muCode,t1.userCode ,t1.userName userName,t1.userCardName userCardName,t1.userMobile userMobile,t1.userAddress userAddress,t1.remark remark,t1.mCode mCode,t1.mName mName,t1.point point,t1.issue issue,t1.addDateTime addDateTime,t1.updateDateTime updateDateTime,t2.userMobile mobile";
		String sqlFrom = " from t_market_user t1 left join t_user t2 on t2.userCode=t1.userCode";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "issue", "=", issue, "and");
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "mCode", "=", mCode, "and");
		String sqlOrder = " order by issue asc,id desc ";
		Page<MarketUser> pages =  MarketUser.marketUserDao.paginate(pageNumber, pageSize, sqlSelect,
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
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
	 * 查询兑换用户列表
	 * @param pageNumber
	 * @param pageSize
	 * @param issue
	 * @param userCode
	 * @return
	 */
	public Map<String,Object> queryExchangeNow() {
		String sqlSelect = "select userName,mName,updateDateTime ";
		String sqlFrom = " from t_market_user";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String sqlOrder = " order by updateDateTime desc";
		Page<MarketUser> pages =  MarketUser.marketUserDao.paginate(1, 12, sqlSelect,
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("list", pages.getList());
		
		return result;
	}

	/**
	 * 查询兑换用户列表  模糊查询
	 * @param pageNumber
	 * @param pageSize
	 * @param userName
	 * @param mName
	 * @return
	 */
	public Page<MarketUser> queryMarketUserLike(Integer pageNumber, Integer pageSize,
			String option) {
		String sqlSelect = "select t1.id id,t1.muCode muCode,t1.userCode ,t1.userName userName,t1.userCardName userCardName,t1.userMobile userMobile,t1.userAddress userAddress,t1.remark remark,t1.mCode mCode,t1.mName mName,t1.point point,t1.issue issue,t1.addDateTime addDateTime,t1.updateDateTime updateDateTime,t2.userMobile mobile";
		String sqlFrom = " from t_market_user t1 left join t_user t2 on t2.userCode=t1.userCode";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		String[] keys = new String[]{"t1.userName","t1.mName","t1.userMobile"};
		makeExp4AnyLike(buff, paras, keys, option, "and","or");
		String sqlOrder = " order by id asc ";
		return   MarketUser.marketUserDao.paginate(pageNumber, pageSize, sqlSelect,sqlFrom
				+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
	}
	
	
	/**
	 * 查询
	 * @param userCode
	 * @return
	 */
	public MarketUser queryDetail(String muCode) {
		return MarketUser.marketUserDao.findFirst(
				"select " + basic_selectFields + " from t_market_user where muCode = ?", muCode);
	}
	
	/**
	 * 查询
	 * @param userCode
	 * @param mCode
	 * @return
	 */
	public MarketUser queryDetail(String userCode , String mCode) {
		return MarketUser.marketUserDao.findFirst("select " + basic_selectFields + " from t_market_user where userCode = ? and mCode = ?",userCode,mCode);
	}
	
	
	
	/**
	 * 新增
	 * @param userCode				用户标识
	 * @param userAddress			地址
	 * @param mCode					商品标识		
	 * @param mName					商品名称
	 * @param point					所需积分
	 * @return
	 */
	public boolean save(MarketUser marketUser){
		marketUser.set("muCode", UIDUtil.generate());
		marketUser.set("issue", 0);
		marketUser.set("addDateTime", DateUtil.getNowDateTime());
		marketUser.set("updateDateTime", DateUtil.getNowDateTime());
		return marketUser.save();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public boolean del(String userCode){
		MarketUser user = queryDetail(userCode);
		 if(null != user){
			return user.delete();
		 }
		 return true;
	}
	
	
	/**
	 * 修改发放状态
	 * @param muCode	标识
	 * @param issue		是否发放 0  未发放  1 已发放
	 * @return
	 */
	public boolean updateIssue(String muCode , String issue){
		int update = Db.update("update t_market_user set issue = ? , updateDateTime = ? where muCode = ?",
			issue , DateUtil.getNowDateTime() , muCode);
		return update > 0 ? true : false ;
	}
	
	public String findMobile(String muCode){
		String  zz = Db.queryStr("select userMobile from t_market_user where muCode = ?",muCode);
		if(StringUtil.isBlank(zz)){
			return null;
		}
		return zz;
	}

}







