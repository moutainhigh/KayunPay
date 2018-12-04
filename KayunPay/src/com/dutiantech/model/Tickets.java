package com.dutiantech.model;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Model;

public class Tickets extends Model<Tickets> {
	//加息额度临时券编码
	public static String rewardRateAomuntTcode="rewardrateamountDyrhx";
	public static String rewardRateInterestTcode="rewardRateInterestTcode";
	public enum STATE{
		A {
			public String key(){
				return "A";
			};
			public String desc(){
				return "可用";
			}
		},
		B {
			public String key(){
				return "B";
			};
			public String desc(){
				return "不可用";
			}
		},
		C {
			public String key(){
				return "C";
			};
			public String desc(){
				return "人工停用";
			}
		},
		D {
			public String key(){
				return "D";
			};
			public String desc(){
				return "过期";
			}
		},
		E {
			public String key(){
				return "E";
			};
			public String desc(){
				return "已使用";
			}
		};
		public abstract String key();
		public abstract String desc();
	}
	
	private static final long serialVersionUID = 7350615595252432223L;
	public static final Tickets ticketsDao = new Tickets();
	
	
	
	/**
	 * 添加抵用券
	 * @param userCode
	 * @param userName
	 * @param userMobile
	 * @param userTrueName
	 * @param tname
	 * @param expDate
	 * @param settingsType
	 * @param opUserCode
	 * @param ms
	 * @return
	 */
	public boolean saveTicket(String userCode, String userName, String userMobile, String userTrueName, 
			String tname, String expDate, String settingsType,String opUserCode,SysEnum.makeSource ms){
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		tickets.set("userMobile", userMobile);tickets.set("userName", userName);tickets.set("userTrueName", userTrueName);
		tickets.set("userCode", userCode);tickets.set("tname", tname);
		tickets.set("expDate", expDate);tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSource", ms.val());
		tickets.set("makeSourceDesc", ms.desc());
		if(StringUtil.isBlank(opUserCode)){
			
			tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		}else{
			tickets.set("makeSourceUser", opUserCode);
		}
		
		String tmpKey = "TT." + settingsType +"_" ;
		int amount = CACHED.getInt(tmpKey + "payAmount");
//		String useEx = (String) CACHED.get("S4.xjdyq_useEx");
		int exAmount = CACHED.getInt(tmpKey + "exAmount");
		int exRate = CACHED.getInt(tmpKey + "exRate");
		int exLimit = CACHED.getInt(tmpKey + "exLimit");
		String exType = CACHED.getStr( tmpKey+"type") ;
		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", exAmount ) ;
		useExObj.put("rate", exRate ) ;
		useExObj.put("limit", exLimit ) ;
		
		tickets.set("tstate", "A");
		tickets.set("amount", amount);
		tickets.set("useEx", useExObj.toJSONString() );
		tickets.set("rate", 0);
		tickets.set("ttype", exType ) ;
		return tickets.save();
	}
	//临时产生一个虚拟券 作为加息额度使用券
	public static Tickets getTmpTickets(){
		Tickets tickets = new Tickets();
		tickets.set("expDate", "20180331");
		tickets.set("tstate", "A");
		tickets.set("amount", 0);
		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", 0 ) ;
		useExObj.put("rate", 0 ) ;
		useExObj.put("limit", 0 ) ;
		tickets.set("useEx", useExObj.toJSONString());
		tickets.set("rate", 100);
		tickets.set("ttype", "C" ) ;
		tickets.set("isDel", "Y" ) ;
		tickets.set("tCode", rewardRateAomuntTcode ) ;
		return tickets;
	}
	
	/**
	 * 临时产生一个虚拟券 作为等级加息使用券
	 * @return
	 */
	public static Tickets getGradeTickets(int rate){
		Tickets tickets = new Tickets();
		tickets.set("expDate", DateUtil.addDay(DateUtil.getNowDate(), 1));
		tickets.set("tstate", "A");
		tickets.set("amount", 0);
		JSONObject useExObj = new JSONObject() ;
		useExObj.put("amount", 0 ) ;
		useExObj.put("rate", 0 ) ;
		useExObj.put("limit", 0 ) ;
		tickets.set("useEx", useExObj.toJSONString());
		tickets.set("rate", rate);
		tickets.set("ttype", "C" ) ;
		tickets.set("isDel", "Y" ) ;
		tickets.set("tCode", rewardRateInterestTcode ) ;
		return tickets;
	}
	
	
}














