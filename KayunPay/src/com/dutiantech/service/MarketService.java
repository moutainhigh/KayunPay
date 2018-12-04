package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.Market;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;


public class MarketService extends BaseService {
	
	private static final String basic_selectFields = " mid,mCode,mName,mPic,mDesc,point,count,remainCount,status,startDateTime,endDateTime,level,levelName,addDateTime,updateDateTime,mRemark,mType ";

	/**
	 * 查询商品列表
	 * @param pageNumber
	 * @param pageSize
	 * @param status
	 * @return
	 */
	public Page<Market> queryMarket(Integer pageNumber, Integer pageSize, String status) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_market";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "status", "=", status, "and");
		String sqlOrder = " order by mid desc ";
		return Market.marketDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}
	
	/**
	 * 查询理财券商品列表
	 * @param mType
	 * @return 
	 */
	public Page<Market> queryMarketLCQByPage(Integer pageNumber, Integer pageSize, String status) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_market";
		StringBuffer buff = new StringBuffer("mType like 'C%'");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "status", "=", status, "and");
		String sqlOrder = " order by point desc ";
		return Market.marketDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}
	
	/**
	 * 查询实物商品列表
	 * @param mType
	 * @return 
	 */
	public Page<Market> queryMarketSWSP(Integer pageNumber, Integer pageSize, String status) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_market";
		StringBuffer buff = new StringBuffer("mType = 'A'");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "status", "=", status, "and");
		String sqlOrder = " order by point asc ";
		return Market.marketDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}
	

	
	/**
	 * 查询电子卡商品列表
	 * @param mType
	 * @return 
	 */
	public Page<Market> queryMarketDZK(Integer pageNumber, Integer pageSize, String status) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_market";
		StringBuffer buff = new StringBuffer("mType = 'B'");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "status", "=", status, "and");
		String sqlOrder = " order by point asc ";
		return Market.marketDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder ,paras.toArray());
	}

	/**
	 * 查询
	 * @param mCode
	 * @return
	 */
	public Market queryMarketDetail(String mCode) {
		Market market = Market.marketDao.findFirst("select " + basic_selectFields + " from t_market where mCode = ?", mCode);
		return market;
	}
	
	
	
	/**
	 * 新增
	 * @param mName				商品名称
	 * @param mPic				商品图片地址
	 * @param mDesc				商品描述
	 * @param point				需要积分
	 * @param count				数量
	 * @param status			状态  0 停用  1 正常
	 * @param startDateTime		兑换开始时间
	 * @param endDateTime		兑换结束时间
	 * @param level				所需等级
	 * @param levelName			所需等级名称
	 * @param mRemark			备注
	 * @return
	 */
	public boolean saveMarket(Market market){
		market.set("mCode", UIDUtil.generate());
		market.set("remainCount", market.getInt("count"));
		market.set("addDateTime", DateUtil.getNowDateTime());
		market.set("updateDateTime", DateUtil.getNowDateTime());
		return market.save();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public boolean delMarket(String mCode){
		 Market market = queryMarketDetail(mCode);
		 if(null != market){
			return market.delete();
		 }
		 return true;
	}
	
	
	/**
	 * 修改商品信息
	 * @param mName				商品名称
	 * @param mPic				商品图片地址
	 * @param mDesc				商品描述
	 * @param point				需要积分
	 * @param count				数量
	 * @param status			状态  0 停用  1 正常
	 * @param startDateTime		兑换开始时间
	 * @param level				所需等级
	 * @param levelName			所需等级名称
	 * @param mRemark			备注
	 * @return
	 */
	public boolean updateMarket(Market market){
		market.remove("addDateTime");
		market.remove("mCode");
		market.set("updateDateTime", DateUtil.getNowDateTime());
		return market.update();
	}
	
	/**
	 * 修改商品剩余数量2017.5.10
	 * @param mCode
	 * @return
	 */
	public boolean updateRemainCountNum(String mCode,int num){
		int update = Db.update("update t_market set remainCount = remainCount - ? where remainCount-? >= 0 and status = 1 and mCode = ? "
				,num,num,mCode);
		return update > 0 ? true : false;
	}
	
	
	/**
	 * 修改商品剩余数量
	 * @param mCode
	 * @return
	 */
	public boolean updateRemainCount(String mCode){
		int update = Db.update("update t_market set remainCount = remainCount - 1 where remainCount-1 >= 0 and status = 1 and mCode = ? "
				,mCode);
		return update > 0 ? true : false;
	}
	

	/**
	 * 修改商品剩余数量
	 * @param mCode
	 * @return
	 */
	public boolean updateRemainCountNoStatus(String mCode){
		int update = Db.update("update t_market set remainCount = remainCount - 1 where remainCount-1 >= 0 and mCode = ? "
				,mCode);
		return update > 0 ? true : false;
	}
	
}







