package com.dutiantech.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.Market;
import com.dutiantech.model.MarketUser;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.service.MarketService;
import com.dutiantech.service.MarketUserService;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.SMSService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class MarketController extends BaseController {
	
	private MarketService marketService = getService(MarketService.class) ;
	private MarketUserService marketUserService = getService(MarketUserService.class) ;
	private RecommendInfoService rmdService = getService(RecommendInfoService.class);
	
	@SuppressWarnings("unused")
	private SMSService sMSService = getService(SMSService.class);
	
	/**
	 * 查询商品列表
	 */
	@ActionKey("/queryMarketByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryMarketByPage(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String status = getPara("status");
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		
		Page<Market> market = marketService.queryMarket(pageNumber, pageSize, status);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	
	@ActionKey("/queryMarketDetail")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryMarketDetail(){
		String mCode = getPara("mCode");
		Market market = marketService.queryMarketDetail(mCode);
		return succ("查询成功", market);
	}
	
	
	
	/**
	 * 添加商品
	 * 	Parameters:
			mName 商品名称						必填
			mPic 商品图片地址					必填
			mDesc 商品描述
			point 需要积分						必填
			count 数量							必填
			status 状态 0 停用 1 正常				默认 0
			startDateTime 兑换开始时间			默认不限
			endDateTime	兑换结束时间				默认不限
			level 所需等级						默认 1
			levelName 所需等级名称				默认少尉
			mRemark 备注
			market 
	 * 
	 * 
	 */
	@ActionKey("/addMarket")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addMarket(){
		Market market = getModel(Market.class, "market");
		if(market == null){
			return error("01", "参数错误", "");
		}
		if(StringUtil.isBlank(market.getStr("mName"))){
			return error("02", "商品名称不能为空", "");
		}
		if(StringUtil.isBlank(market.getStr("mPic"))){
			return error("03", "商品图片地址不能为空", "");
		}
		if(market.getLong("point") == null || market.getLong("point") <= 0){
			return error("04", "兑换需要积分错误", "");
		}
		if(market.getInt("count") == null || market.getInt("count") <= 0){
			return error("05", "商品总数量错误", "");
		}
		if(StringUtil.isBlank(market.getStr("status"))){
			market.set("status", 0);
		}
		if(StringUtil.isBlank(market.getStr("startDateTime"))){
			market.set("startDateTime", "00000000000000");
		}
		if(StringUtil.isBlank(market.getStr("endDateTime"))){
			market.set("endDateTime", "99999999999999");
		}
		if(market.getInt("level") == null || market.getInt("level") <= 0){
			market.set("level", 1);
		}
		if(StringUtil.isBlank(market.getStr("levelName"))){
			market.set("levelName", "少尉");
		}
		
		boolean saveMarket = marketService.saveMarket(market);
		
		if(saveMarket == false){
			return error("06", "保存异常", "");
		}
		return succ("保存成功", null ) ;
	}
	
	
	/**
	 * 删除商品
	 * @return
	 */
	@ActionKey("/delMarket")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message delMarket(){
		String mCode = getPara("mCode");
		if(StringUtil.isBlank(mCode)){
			return error("01", "参数错误", "");
		}
		boolean delMarket = marketService.delMarket(mCode);
		if(delMarket == false){
			return error("02", "删除异常", "");
		}
		return succ("删除成功", null ) ;
	}
	
	
	/**
	 * 修改
 	 * 	Parameters:
		mName 商品名称						必填
		mPic 商品图片地址					必填
		mDesc 商品描述
		point 需要积分						必填
		count 数量							必填
		status 状态 0 停用 1 正常				默认 0
		startDateTime 兑换开始时间			默认不限
		endDateTime	兑换结束时间				默认不限
		level 所需等级						默认 1
		levelName 所需等级名称				默认少尉
		mRemark 备注
		market 
	 * @return
	 */
	@ActionKey("/updateMarket")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateMarket(){
		Market market = getModel(Market.class, "market");
		if(market == null){
			return error("01", "参数错误", "");
		}
		if(market.getLong("mid") == null){
			return error("02", "商品ID不能为空", "");
		}
		if(StringUtil.isBlank(market.getStr("mCode"))){
			return error("02", "商品编号不能为空", "");
		}
		if(StringUtil.isBlank(market.getStr("mName"))){
			return error("03", "商品名称不能为空", "");
		}
		if(StringUtil.isBlank(market.getStr("mPic"))){
			return error("04", "商品图片地址不能为空", "");
		}
		if(market.getLong("point") == null || market.getLong("point") <= 0){
			return error("05", "兑换需要积分错误", "");
		}
		if(market.getInt("count") == null || market.getInt("count") <= 0){
			return error("06", "商品总数量错误", "");
		}
		if(StringUtil.isBlank(market.getStr("status"))){
			market.set("status", "0");
		}
		if(StringUtil.isBlank(market.getStr("startDateTime"))){
			market.set("startDateTime", "00000000000000");
		}
		if(StringUtil.isBlank(market.getStr("endDateTime"))){
			market.set("endDateTime", "99999999999999");
		}
		if(market.getInt("level") == null || market.getInt("level") <= 0){
			market.set("level", 1);
		}
		if(StringUtil.isBlank(market.getStr("levelName"))){
			market.set("levelName", "少尉");
		}
		
		boolean saveNotice = marketService.updateMarket(market);
		if(saveNotice == false){
			return error("07", "修改异常", "");
		}
		return succ("修改成功", null ) ;
	}
	
	
	/**
	 * 查询兑换列表
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/queryExchange")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryExchange(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String userCode = getPara("userCode");
		String mCode = getPara("mCode");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		
		Map<String,Object> result = marketUserService.queryMarketUser(pageNumber, pageSize, null, userCode,mCode);
		ArrayList marketUserList= (ArrayList) result.get("list");
		for(int i=0;i<marketUserList.size();i++){
              MarketUser marketUser=(MarketUser) marketUserList.get(i);
              try {
            	  marketUser.put("mobile", CommonUtil.decryptUserMobile(marketUser.getStr("mobile")));
            	  result.put("list", marketUserList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			  }
		return succ("查询成功!", result);
	}
	
	
	/**
	 * 查询兑换列表 模糊查询
	 */
	@ActionKey("/queryExchangeLike")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryExchangeLike(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String option = getPara("option");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		Page<MarketUser> marketUser = marketUserService.queryMarketUserLike(pageNumber, pageSize, option);
		 List<MarketUser> list = marketUser.getList();
		 for(int i=0;i<list.size();i++){
			 MarketUser marketUser1 = (MarketUser) list.get(i);
             try {
           	  marketUser1.put("mobile", CommonUtil.decryptUserMobile(marketUser1.getStr("mobile")));  
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			  }
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("marketUser", marketUser);
		return succ("查询成功!", map);
	}
	
	
	
	/**
	 * 发放奖品
	 */
	@ActionKey("/issue")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message issue(){
		String muCode = getPara("muCode");
		String msgInfo = getPara("msgInfo");
		if(StringUtil.isBlank(muCode)){
			return error("01", "参数错误", "");
		}
		if(StringUtil.isBlank(msgInfo)){
			return error("03", "请填写奖品信息啊！", "");
		}
		String mobile = marketUserService.findMobile(muCode);
		int zhazha = -1;
		boolean z = CommonUtil.isMobile(mobile);
		if(z){
			zhazha = SMSClient.sendSms(mobile, msgInfo);
//			zhazha = sMSService.sendSms(mobile, msgInfo);
			if(zhazha==0){
				boolean zz = marketUserService.updateIssue(muCode, "1");
				if(zz){
					return succ("发放成功，短信已通知到位！", true);
				}else{
					return error("GG", "短信已发送,但系统发生异常未更新是否发放状态,编号["+muCode+"]",muCode);
				}
			}
		}else{
			return error("GG", "发送短信失败！返回码["+zhazha+"]", zhazha);
		}
		
		
		return error("GG", "发送短信失败！返回码["+zhazha+"]", zhazha);
	}
	
	
	/**
	 * 查询推荐列表
	 */
	@ActionKey("/queryShareLike")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryShareLike(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String option = getPara("option");
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		Page<RecommendInfo> pages = rmdService.queryShareListLike(pageNumber, pageSize, option);
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return succ("查询成功", result);
	}
	
	/**
	 * 七夕发奖，临时
	 */
	@ActionKey("/qixifajiang")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message qixifajiang(){
		String key = getPara("key", "");
		if(!key.equals("3.14159265358")){
			return error("01","密匙错误", false );
		}
		int _10 = 0; int _20=0; int _50 = 0;
		List<MarketUser> marketUsers = MarketUser.marketUserDao.find("select * from t_market_user where issue = '0' and mCode in ('5d4b533384d34af6a4bbf27e36eed51c','a2411f9488f44cb0ae50428a69ec5cd1','e98ba863f2b44039908d4dd7a7baaeb4')");
		for (int i = 0; i < marketUsers.size(); i++) {
			MarketUser mu = marketUsers.get(i);
			String muCode = mu.getStr("muCode");
			String userCode = mu.getStr("userCode");
			String userName = mu.getStr("userName");
			String userCardName = mu.getStr("userCardName");
			String mCode = mu.getStr("mCode");
			boolean isOk = false;
			if(mCode.equals("5d4b533384d34af6a4bbf27e36eed51c")){//10元现金券
				if(made10(userCode, userName, userCardName)){
					_10++;
					isOk = true;
				}
					
			}else if(mCode.equals("a2411f9488f44cb0ae50428a69ec5cd1")){//20元现金券
				if(made20(userCode, userName, userCardName)){
					isOk = true;_20++;
				}
			}else if(mCode.equals("e98ba863f2b44039908d4dd7a7baaeb4")){//50元现金券
				if(made50(userCode, userName, userCardName)){
					isOk = true;_50++;
				}
					
			}
			if(isOk){
				Db.update("update t_market_user set issue = '1' where muCode = ?",muCode);
			}
		}
		System.out.println("七夕发券任务完成：共"+(_10+_20+_50)+"条，其中10元奖券"+_10+"条,20元奖券"+_20+"条,50元奖券"+_50+"条");
		return succ("操作完成", null);
	}
	
	
	private boolean made10(String userCode, String userName,String userCardName){
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		String userMobile = "13800000000";
		try {
			userMobile = User.userDao.findByIdLoadColumns(userCode, "userCode,userMobile").getStr("userMobile");
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			if(CommonUtil.isMobile(userMobile) == false){
				userMobile = "13800000000";
			}
		} catch (Exception e) {
			userMobile = "13800000000";
		}
		tickets.set("userMobile", userMobile);tickets.set("userName", userName);tickets.set("userTrueName", userCardName);
		tickets.set("userCode", userCode);tickets.set("tname", "10元现金券【七夕心动礼】");
		tickets.set("expDate", "20160815");tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSource", "B");
		tickets.set("makeSourceDesc", "活动");
		tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", 100000 ) ;
		useExObj.put("rate", 0 ) ;
		useExObj.put("limit", 0 ) ;
		
		tickets.set("tstate", "A");
		tickets.set("amount", 1000);
		tickets.set("useEx", useExObj.toJSONString() );
		tickets.set("rate", 0);
		tickets.set("ttype", "A" ) ;
		return tickets.save();
	}
	
	private boolean made20(String userCode, String userName,String userCardName){
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		String userMobile = "13800000000";
		try {
			userMobile = User.userDao.findByIdLoadColumns(userCode, "userCode,userMobile").getStr("userMobile");
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			if(CommonUtil.isMobile(userMobile) == false){
				userMobile = "13800000000";
			}
		} catch (Exception e) {
			userMobile = "13800000000";
		}
		tickets.set("userMobile", userMobile);tickets.set("userName", userName);tickets.set("userTrueName", userCardName);
		tickets.set("userCode", userCode);tickets.set("tname", "20元现金券【七夕约会礼】");
		tickets.set("expDate", "20160815");tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSource", "B");
		tickets.set("makeSourceDesc", "活动");
		tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", 300000 ) ;
		useExObj.put("rate", 0 ) ;
		useExObj.put("limit", 0 ) ;
		
		tickets.set("tstate", "A");
		tickets.set("amount", 2000);
		tickets.set("useEx", useExObj.toJSONString() );
		tickets.set("rate", 0);
		tickets.set("ttype", "A" ) ;
		return tickets.save();
	}
	
	private boolean made50(String userCode, String userName, String userCardName){
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		String userMobile = "13800000000";
		try {
			userMobile = User.userDao.findByIdLoadColumns(userCode, "userCode,userMobile").getStr("userMobile");
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			if(CommonUtil.isMobile(userMobile) == false){
				userMobile = "13800000000";
			}
		} catch (Exception e) {
			userMobile = "13800000000";
		}
		tickets.set("userMobile", userMobile);tickets.set("userName", userName);tickets.set("userTrueName", userCardName);
		tickets.set("userCode", userCode);tickets.set("tname", "50元现金券【七夕甜蜜礼】");
		tickets.set("expDate", "20160815");tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSource", "B");
		tickets.set("makeSourceDesc", "活动");
		tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", 1000000 ) ;
		useExObj.put("rate", 0 ) ;
		useExObj.put("limit", 0 ) ;
		
		tickets.set("tstate", "A");
		tickets.set("amount", 5000);
		tickets.set("useEx", useExObj.toJSONString() );
		tickets.set("rate", 0);
		tickets.set("ttype", "A" ) ;
		return tickets.save();
	}
	
	
}