package com.dutiantech.controller.admin;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.PrizeRecords;
import com.dutiantech.model.Prizes;
import com.dutiantech.model.Tickets;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

/**
 * 奖品发放任务
 * @author shiqingsong
 *
 */
public class LotteryTaskController extends BaseController {

	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private static final Logger lotteryTaskController = Logger.getLogger("lotteryTaskController");
	
	static{
		LoggerUtil.initLogger("lotteryTaskController", lotteryTaskController);
	}
	
	/**
	 * 循环处理虚拟奖品发奖
	 * @return
	 */
	@ActionKey("/handleLottery")
	@Before( PkMsgInterceptor.class )
	public Message handleLottery(){
		
		lotteryTaskController.log(Level.INFO,"开始扫描待处理奖品...");
		Page<PrizeRecords> prizeRecordPage = PrizeRecords.prizeRecordsDao.queryNoDispose();
		if(null != prizeRecordPage && null != prizeRecordPage.getList()){
			lotteryTaskController.log(Level.INFO,"开始发放虚拟奖品...");
			for (int i = 0; i < prizeRecordPage.getList().size(); i++) {
				try{
					PrizeRecords prizeRecords = prizeRecordPage.getList().get(i);
					int prizeLevel = prizeRecords.getInt("prizeLevel");
					String userCode = prizeRecords.getStr("userCode");
					String userName = prizeRecords.getStr("userName");
					String prizeType = prizeRecords.getStr("prizeType");
					String remark = prizeRecords.getStr("remark");
					if(1 == prizeLevel){
						//现金券
						String exDate = DateUtil.addDay(DateUtil.getNowDate(), prizeRecords.getInt("exDateTime"));
						Tickets.ticketsDao.saveTicket(userCode, userName, "", "", 
								remark, exDate , prizeType, null, SysEnum.makeSource.A);
					}else if (2 == prizeLevel) {
						//积分
						long amount = prizeRecords.getLong("amount");
						fundsServiceV2.doPoints(userCode, 0, amount, remark);
					}
					
					//修改奖品记录状态
					PrizeRecords.prizeRecordsDao.updateRecordStatus(prizeRecords.getStr("recordCode"));
					
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			
		}
		return succ("发奖完成", "");
	}
	
	/**
	 * 初始化商品
	 * @return
	 */
	@ActionKey("/initPrize")
	@Before( PkMsgInterceptor.class )
	public Message initPrize(){
		
		lotteryTaskController.log(Level.INFO,"开始初始化奖品数量...");
		
		String initLottery = CACHED.getStr("AT.initLottery");
		String activeCode = CACHED.getStr("AT.activeCode");
		
		Prizes.prizeDao.initPrizeCount(activeCode);
		
		HttpRequestUtil.sendGet(initLottery, null);
		return succ("奖品数量初始化成功", "");
		
	}
	
}














