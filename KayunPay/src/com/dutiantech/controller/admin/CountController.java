package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutian.sohusdk.Mail;
import com.dutian.sohusdk.Sender;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.PlatformCount;
import com.dutiantech.model.UserCount;
import com.dutiantech.model.ViewSysCount;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.PlatformCountService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdentityUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
/**
 * 统计
 * @author shiqingsong
 *
 */
public class CountController extends BaseController {
	
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private PlatformCountService platformCountService = getService(PlatformCountService.class);
	
	
	private UserCount setArea_Amount(UserCount uc,String userCardId,String userCode){
		String area_num = userCardId.substring(0,2);
		Integer oldData1 = uc.getInt("area_"+area_num);
		if(oldData1 ==null){
			uc.set("area_"+area_num, 1);
		}else{
			uc.set("area_"+area_num, 1+oldData1);
		}
		long amt = Db.queryBigDecimal("select COALESCE(SUM(payAmount),0) from t_loan_trace where loanState in('N','O','P') and payUserCode = ?",userCode).longValue();
		Long oldData2 = uc.getLong("amt_"+area_num);
		if(oldData2 ==null){
			uc.set("amt_"+area_num, amt);
		}else{
			uc.set("amt_"+area_num, amt+oldData2.longValue());
		}
		return uc;
	}
	
	/**
	 * 用户统计(区域人数分布、区域投资金额、性别、年龄)
	 */
	@ActionKey("/madeUserInfoData")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message madeUserInfoData(){
		
		if(getPara("key","").equals("3.14159265358") == false){
			return error("01", "密匙不对伙计", null);
		}
		
		UserCount uc = new UserCount();
		uc.set("countDate", DateUtil.getNowDate());
		uc.set("countTime", DateUtil.getNowTime());
		List<Object[]> objs = Db.query("select t2.userCardId,t1.userCode from t_user t1 inner join t_user_info t2 on t1.userCode = t2.userCode where t1.userCode not in (select userCode from t_loan_info)");
		for (int i = 0; i < objs.size(); i++) {
			try {
				String userCardId = (String) objs.get(i)[0];
				String userCode = (String) objs.get(i)[1];
				try {
					userCardId = CommonUtil.decryptUserCardId(userCardId);
				} catch (Exception e) {
					userCardId = "";
				}
				if(StringUtil.isBlank(userCardId))
					continue;
				
				
				//港澳居民来往内地通行证号码,只统计区域人数和区域投资金额，无法统计性别年龄
				if(userCardId.length() == 11 || userCardId.length()==10){
					//验证【港澳居民来往内地通行证】
					if(IdentityUtil.validateHMPassCard(userCardId) == false)
						continue;
					if(userCardId.startsWith("H")){
						uc = setArea_Amount(uc, "81xxx",userCode);
					}else if(userCardId.startsWith("M")){
						uc = setArea_Amount(uc, "82xxx",userCode);
					}
				//台湾来往大陆通行证号码
				}else if(userCardId.length()==8 ){
					//验证【台湾来往大陆通行证】
					if(IdentityUtil.validateTWPassCard(userCardId) == false)
						continue;
					uc = setArea_Amount(uc, "71xxx",userCode);
				//大陆公民身份证号码，统计区域人数、区域投资金额和年龄、性别
				}else if(userCardId.length() == 15 || userCardId.length() == 18){
					if(userCardId.length()==15)
						userCardId = IdentityUtil.convertIdcarBy15bit(userCardId);
					//验证身份证合法性
					if(IdentityUtil.vilidateCardId(userCardId) == false)
						continue;
					
					uc = setArea_Amount(uc, userCardId,userCode);
					
					int age = IdentityUtil.getAge(userCardId);
					if(age>=10 && age<=19){
						Integer oldAge10_19 = uc.getInt("age10_19");
						if(oldAge10_19 == null)
							uc.set("age10_19", 1);
						else
							uc.set("age10_19", oldAge10_19 + 1);
					}else if(age >= 20 && age <= 29){
						Integer oldAge20_29 = uc.getInt("age20_29");
						if(oldAge20_29 == null)
							uc.set("age20_29", 1);
						else
							uc.set("age20_29", oldAge20_29 + 1);
					}else if(age >= 30 && age <= 39){
						Integer oldAge30_39 = uc.getInt("age30_39");
						if(oldAge30_39 == null)
							uc.set("age30_39", 1);
						else
							uc.set("age30_39", oldAge30_39 + 1);
					}else if(age >= 40 && age <= 49){
						Integer oldAge40_49 = uc.getInt("age40_49");
						if(oldAge40_49 == null)
							uc.set("age40_49", 1);
						else
							uc.set("age40_49", oldAge40_49 + 1);
					}else if(age >= 50 && age <= 59){
						Integer oldAge50_59 = uc.getInt("age50_59");
						if(oldAge50_59 == null)
							uc.set("age50_59", 1);
						else
							uc.set("age50_59", oldAge50_59 + 1);
					}else if(age >= 60 && age <= 120){
						Integer oldAge60_150 = uc.getInt("age60_150");
						if(oldAge60_150 == null)
							uc.set("age60_150", 1);
						else
							uc.set("age60_150", oldAge60_150 + 1);
					}
					
					if(Integer.parseInt(userCardId.substring(16).substring(0, 1)) % 2 == 0){
						Integer oldSexF = uc.getInt("sexF");
						if(oldSexF == null)
							uc.set("sexF", 1);
						else
							uc.set("sexF", oldSexF + 1);
					}else{
						Integer oldSexM = uc.getInt("sexM");
						if(oldSexM == null)
							uc.set("sexM", 1);
						else
							uc.set("sexM", oldSexM + 1);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("统计用户性别、年龄、区域、投资金额异常，不统计了："+e.getMessage());
				return error("98", "统计用户性别、年龄、区域、投资金额异常，不统计了："+e.getMessage(), null);
			}
		}
		Db.update("delete from t_user_count where countDate = ?",DateUtil.getNowDate());
		uc.save();
		return succ("统计完成", "ok");
	}
	
	
	/**
	 * 查询后台首页统计预览
	 */
	@ActionKey("/queryCountAdmin")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="ADMIN_FIRSTPAGE_COUNT",mode="local",time=10*60, cachedKeyParm = "")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryNewsByPage(){
		ViewSysCount viewSysCount = ViewSysCount.viewSysDao.findFirst("select * from view_syscount");
		long totalBeAmount = loanInfoService.totalBeAmount();//待发布总额
		long totalRelAmount = loanInfoService.totalRelAmount();//今日发布总额
		long totalSuccRelAmount = loanInfoService.totalSuccRelAmount();//今日已满标总额
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("viewSysCount", viewSysCount);
		map.put("totalBeAmount", totalBeAmount);
		map.put("totalRelAmount", totalRelAmount);
		map.put("totalSuccRelAmount", totalSuccRelAmount);
		Message msg = succ("查询成功", map);
		renderJson(msg);
	}
	
	/**
	 * 每日平台数据统计0:01触发，统计昨天的数据，并初始化今天的数据
	 */
	@ActionKey("/fuckPlatformData")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message fuckPlatformData(){
		if(getPara("key","").equals("3.14159265358") == false){
			return error("01", "密匙不对伙计", null);
		}
		//生成昨天平台统计数据
		String yesterday = DateUtil.delDay(DateUtil.getNowDate(), 1);
		boolean xx = platformCountService.madePlatformData(yesterday);
		
		//发他妈的邮件给一起好
		if(xx){
			PlatformCount pfc = PlatformCount.platformCountDao.findById(yesterday);
			if(pfc!=null){
				Map<String, String> params = new HashMap<String, String>();
				params.put("countDate", DateUtil.getStrFromDate(DateUtil.getDateFromString(yesterday,"yyyyMMdd"), "yyyy年MM月dd日"));
				//沉淀资金
				params.put("cdzj",CommonUtil.yunsuan((pfc.getLong("kyye")+pfc.getLong("djye"))+"", "1000000", "chu", 2)+"万");
				//可用余额
				params.put("kyye",CommonUtil.yunsuan(pfc.getLong("kyye")+"", "1000000", "chu", 2)+"万");
				//冻结余额
				params.put("djye",CommonUtil.yunsuan(pfc.getLong("djye")+"", "1000000", "chu", 2)+"万");
				//充值总额
				params.put("czze",CommonUtil.yunsuan((pfc.getLong("rgcz")+pfc.getLong("llzfcz"))+pfc.getLong("syxcz")+"", "1000000", "chu", 2)+"万");
				//人工充值
				params.put("rgcz",CommonUtil.yunsuan(pfc.getLong("rgcz")+"", "1000000", "chu", 2)+"万");
				//连连支付充值
				params.put("llzfcz",CommonUtil.yunsuan(pfc.getLong("llzfcz")+"", "1000000", "chu", 2)+"万");
				//商银信充值
				params.put("syxcz",CommonUtil.yunsuan(pfc.getLong("syxcz")+"", "1000000", "chu", 2)+"万");
				//今日成交总额
				params.put("jrcjze",CommonUtil.yunsuan(pfc.getLong("jrcjze")+"", "1000000", "chu", 2)+"万");
				//提现总额
				params.put("txze",CommonUtil.yunsuan((pfc.getLong("rgtx")+pfc.getLong("llzftx"))+pfc.getLong("syxtx")+"", "1000000", "chu", 2)+"万");
				//人工提现
				params.put("rgtx",CommonUtil.yunsuan(pfc.getLong("rgtx")+"", "1000000", "chu", 2)+"万");
				//连连支付提现
				params.put("llzftx",CommonUtil.yunsuan(pfc.getLong("llzftx")+"", "1000000", "chu", 2)+"万");
				//商银信提现
				params.put("syxtx",CommonUtil.yunsuan(pfc.getLong("syxtx")+"", "1000000", "chu", 2)+"万");
				//应还总额
				params.put("yhze",CommonUtil.yunsuan((pfc.getLong("yhbj")+pfc.getLong("yhlx"))+"", "1000000", "chu", 2)+"万");
				//待还总额
				params.put("dhze","0.00万");
				//应还本金
				params.put("yhbj",CommonUtil.yunsuan(pfc.getLong("yhbj")+"", "1000000", "chu", 2)+"万");
				//待还本金
				params.put("dhbj","0.00万");
				//应还利息
				params.put("yhlx",CommonUtil.yunsuan(pfc.getLong("yhlx")+"", "1000000", "chu", 2)+"万");
				//待还利息
				params.put("dhlx","0.00万");
				//实际还款总额
				params.put("sjhkze", CommonUtil.yunsuan((pfc.getLong("sjhkbj")+pfc.getLong("sjhklx"))+"", "1000000", "chu", 2)+"万");
				//实际还款本金
				params.put("sjhkbj",CommonUtil.yunsuan(pfc.getLong("sjhkbj")+"", "1000000", "chu", 2)+"万");
				//实际还款利息
				params.put("sjhklx",CommonUtil.yunsuan(pfc.getLong("sjhklx")+"", "1000000", "chu", 2)+"万");
				//提前还款本金
				params.put("tqhkbj",CommonUtil.yunsuan(pfc.getLong("tqhkbj")+"", "1000000", "chu", 2)+"万");
				//累计成交金额
				params.put("ljcjze",CommonUtil.yunsuan(pfc.getLong("ljcjze")+"", "1000000", "chu", 2)+"万");
				//累计赚取收益
				params.put("ljzq",CommonUtil.yunsuan(pfc.getLong("ljzq")+"", "1000000", "chu", 2)+"万");
				//1-3月标成交金额
				params.put("m13",CommonUtil.yunsuan(pfc.getLong("m13")+"", "1000000", "chu", 2)+"万");
				//4-6月标成交金额
				params.put("m46",CommonUtil.yunsuan(pfc.getLong("m46")+"", "1000000", "chu", 2)+"万");
				//7-12月标成交金额
				params.put("m712",CommonUtil.yunsuan(pfc.getLong("m712")+"", "1000000", "chu", 2)+"万");
				//13-18月标成交金额
				params.put("m1318",CommonUtil.yunsuan(pfc.getLong("m1318")+"", "1000000", "chu", 2)+"万");
				//质押宝成交金额
				params.put("pa",CommonUtil.yunsuan(pfc.getLong("pa")+"", "1000000", "chu", 2)+"万");
				//车稳赢成交金额
				params.put("pb",CommonUtil.yunsuan(pfc.getLong("pb")+"", "1000000", "chu", 2)+"万");
				//房稳赚成交金额
				params.put("pc",CommonUtil.yunsuan(pfc.getLong("pc")+"", "1000000", "chu", 2)+"万");
				//新手标成交金额
				params.put("noob",CommonUtil.yunsuan(pfc.getLong("noob")+"", "1000000", "chu", 2)+"万");
				//本日债权转让承接金额
				params.put("jrzqzr",CommonUtil.yunsuan(pfc.getLong("jrzqzr")+"", "1000000", "chu", 2)+"万");
				//累计债权转让承接金额
				params.put("ljzqzr",CommonUtil.yunsuan(pfc.getLong("ljzqzr")+"", "1000000", "chu", 2)+"万");
				//应收本息
				params.put("ysbx", CommonUtil.yunsuan((pfc.getLong("ysbj")+pfc.getLong("yslx"))+"", "1000000", "chu", 2)+"万");
				//实际收益本金
				params.put("ssbj",CommonUtil.yunsuan(pfc.getLong("sjhkbj")+"", "1000000", "chu", 2)+"万");
				//实际收益利息
				params.put("sslx",CommonUtil.yunsuan(pfc.getLong("sjhklx")+"", "1000000", "chu", 2)+"万");
				//平台累计待收本息
				params.put("zjds",CommonUtil.yunsuan(pfc.getLong("zjds")+"", "1000000", "chu", 2)+"万");
				//风险备付金
				params.put("fxbfj",CommonUtil.yunsuan(pfc.getLong("fxbfj")+"", "1000000", "chu", 2)+"万");
				Sender sender = new Sender();
				String tmp_toEmails = (String) CACHED.get("S1.countDataEmail");
//				tmp_toEmails = "6134642@qq.com";
				String[] toEmails = tmp_toEmails.split(",");
				for (int i = 0; i < toEmails.length; i++) {
					Mail mail = new Mail();
					mail.setTo(toEmails[i]);
					mail.setParams(params);
					sender.putMail(mail);
				}
				sender.setTemplate_invoke_name("22170_wow_99");
				sender.sendMail();
			}
			
		}
		
		//初始化今天的统计数据（包含应还本息）
		platformCountService.initRecord(DateUtil.getNowDate());
		return succ("每日平台数据统计处理完成", true);
	}
	
	/**
	 * 测试统计邮件
	 */
	@ActionKey("/testPD")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message testPD(){
		if(getPara("key","").equals("3.14159265358") == false){
			return error("01", "密匙不对伙计", null);
		}
		//生成昨天平台统计数据
		String yesterday = DateUtil.delDay(DateUtil.getNowDate(), 1);
		boolean xx = platformCountService.madePlatformData(yesterday);
		
		//发他妈的邮件给一起好
		if(xx){
			PlatformCount pfc = PlatformCount.platformCountDao.findById(yesterday);
			if(pfc!=null){
				Map<String, String> params = new HashMap<String, String>();
				params.put("countDate", DateUtil.getStrFromDate(DateUtil.getDateFromString(yesterday,"yyyyMMdd"), "yyyy年MM月dd日"));
				//沉淀资金
				params.put("cdzj",CommonUtil.yunsuan((pfc.getLong("kyye")+pfc.getLong("djye"))+"", "1000000", "chu", 2)+"万");
				//可用余额
				params.put("kyye",CommonUtil.yunsuan(pfc.getLong("kyye")+"", "1000000", "chu", 2)+"万");
				//冻结余额
				params.put("djye",CommonUtil.yunsuan(pfc.getLong("djye")+"", "1000000", "chu", 2)+"万");
				//充值总额
				params.put("czze",CommonUtil.yunsuan((pfc.getLong("rgcz")+pfc.getLong("llzfcz"))+pfc.getLong("syxcz")+"", "1000000", "chu", 2)+"万");
				//人工充值
				params.put("rgcz",CommonUtil.yunsuan(pfc.getLong("rgcz")+"", "1000000", "chu", 2)+"万");
				//连连支付充值
				params.put("llzfcz",CommonUtil.yunsuan(pfc.getLong("llzfcz")+"", "1000000", "chu", 2)+"万");
				//商银信充值
				params.put("syxcz",CommonUtil.yunsuan(pfc.getLong("syxcz")+"", "1000000", "chu", 2)+"万");
				//今日成交总额
				params.put("jrcjze",CommonUtil.yunsuan(pfc.getLong("jrcjze")+"", "1000000", "chu", 2)+"万");
				//提现总额
				params.put("txze",CommonUtil.yunsuan((pfc.getLong("rgtx")+pfc.getLong("llzftx"))+pfc.getLong("syxtx")+"", "1000000", "chu", 2)+"万");
				//人工提现
				params.put("rgtx",CommonUtil.yunsuan(pfc.getLong("rgtx")+"", "1000000", "chu", 2)+"万");
				//连连支付提现
				params.put("llzftx",CommonUtil.yunsuan(pfc.getLong("llzftx")+"", "1000000", "chu", 2)+"万");
				//商银信提现
				params.put("syxtx",CommonUtil.yunsuan(pfc.getLong("syxtx")+"", "1000000", "chu", 2)+"万");
				//应还总额
				params.put("yhze",CommonUtil.yunsuan((pfc.getLong("yhbj")+pfc.getLong("yhlx"))+"", "1000000", "chu", 2)+"万");
				//待还总额
				params.put("dhze","0.00万");
				//应还本金
				params.put("yhbj",CommonUtil.yunsuan(pfc.getLong("yhbj")+"", "1000000", "chu", 2)+"万");
				//待还本金
				params.put("dhbj","0.00万");
				//应还利息
				params.put("yhlx",CommonUtil.yunsuan(pfc.getLong("yhlx")+"", "1000000", "chu", 2)+"万");
				//待还利息
				params.put("dhlx","0.00万");
				//实际还款总额
				params.put("sjhkze", CommonUtil.yunsuan((pfc.getLong("sjhkbj")+pfc.getLong("sjhklx"))+"", "1000000", "chu", 2)+"万");
				//实际还款本金
				params.put("sjhkbj",CommonUtil.yunsuan(pfc.getLong("sjhkbj")+"", "1000000", "chu", 2)+"万");
				//实际还款利息
				params.put("sjhklx",CommonUtil.yunsuan(pfc.getLong("sjhklx")+"", "1000000", "chu", 2)+"万");
				//提前还款本金
				params.put("tqhkbj",CommonUtil.yunsuan(pfc.getLong("tqhkbj")+"", "1000000", "chu", 2)+"万");
				//累计成交金额
				params.put("ljcjze",CommonUtil.yunsuan(pfc.getLong("ljcjze")+"", "1000000", "chu", 2)+"万");
				//累计赚取收益
				params.put("ljzq",CommonUtil.yunsuan(pfc.getLong("ljzq")+"", "1000000", "chu", 2)+"万");
				//1-3月标成交金额
				params.put("m13",CommonUtil.yunsuan(pfc.getLong("m13")+"", "1000000", "chu", 2)+"万");
				//4-6月标成交金额
				params.put("m46",CommonUtil.yunsuan(pfc.getLong("m46")+"", "1000000", "chu", 2)+"万");
				//7-12月标成交金额
				params.put("m712",CommonUtil.yunsuan(pfc.getLong("m712")+"", "1000000", "chu", 2)+"万");
				//13-18月标成交金额
				params.put("m1318",CommonUtil.yunsuan(pfc.getLong("m1318")+"", "1000000", "chu", 2)+"万");
				//质押宝成交金额
				params.put("pa",CommonUtil.yunsuan(pfc.getLong("pa")+"", "1000000", "chu", 2)+"万");
				//车稳赢成交金额
				params.put("pb",CommonUtil.yunsuan(pfc.getLong("pb")+"", "1000000", "chu", 2)+"万");
				//房稳赚成交金额
				params.put("pc",CommonUtil.yunsuan(pfc.getLong("pc")+"", "1000000", "chu", 2)+"万");
				//新手标成交金额
				params.put("noob",CommonUtil.yunsuan(pfc.getLong("noob")+"", "1000000", "chu", 2)+"万");
				//本日债权转让承接金额
				params.put("jrzqzr",CommonUtil.yunsuan(pfc.getLong("jrzqzr")+"", "1000000", "chu", 2)+"万");
				//累计债权转让承接金额
				params.put("ljzqzr",CommonUtil.yunsuan(pfc.getLong("ljzqzr")+"", "1000000", "chu", 2)+"万");
				//应收本息
				params.put("ysbx", CommonUtil.yunsuan((pfc.getLong("ysbj")+pfc.getLong("yslx"))+"", "1000000", "chu", 2)+"万");
				//实际收益本金
				params.put("ssbj",CommonUtil.yunsuan(pfc.getLong("sjhkbj")+"", "1000000", "chu", 2)+"万");
				//实际收益利息
				params.put("sslx",CommonUtil.yunsuan(pfc.getLong("sjhklx")+"", "1000000", "chu", 2)+"万");
				//平台累计待收本息
				params.put("zjds",CommonUtil.yunsuan(pfc.getLong("zjds")+"", "1000000", "chu", 2)+"万");
				//风险备付金
				params.put("fxbfj",CommonUtil.yunsuan(pfc.getLong("fxbfj")+"", "1000000", "chu", 2)+"万");
				Sender sender = new Sender();
//				String tmp_toEmails = (String) CACHED.get("S1.countDataEmail");
				String tmp_toEmails = "6134642@qq.com";
				Mail mail = new Mail();
				mail.setTo(tmp_toEmails);
				mail.setParams(params);
				sender.putMail(mail);
				sender.setTemplate_invoke_name("22170_wow_99");
				sender.sendMail();
			}
			
		}
		return succ("每日平台数据统计处理完成", true);
	}
	
}
