package com.dutiantech.controller.admin;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.TransferInfo;
import com.dutiantech.service.TransferInfoService;
import com.dutiantech.util.CommonUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
@Before(value=AuthInterceptor.class)
public class TransferInfoController extends BaseController{
	
	private TransferInfoService transferInfoService=getService(TransferInfoService.class);
	
	/**
	 * 分页查询后台债权人
	 * @return
	 */
	@ActionKey("/queryTransferByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryTransferByPage(){
		//获取当前页码、每页显示数量
		Integer pageNumber = getParaToInt("pageNumber", 1);
		pageNumber=pageNumber > 0 ? pageNumber:1;
		Integer pageSize=getParaToInt("pageSize", 10);
		
		//获取模糊查询信息
		//allTransfer：债权人     allArea：所属区域
		String allArea=getPara("allArea","");
		String allTransfer=getPara("allTransfer","");
		
		Page<TransferInfo> transferInfos = transferInfoService.findByPage(pageNumber, pageSize, allArea,allTransfer);
		
		if(pageNumber>transferInfos.getTotalPage()&&transferInfos.getTotalPage()>0){
			pageNumber=transferInfos.getTotalPage();
			transferInfos=transferInfoService.findByPage(pageNumber, pageSize, allArea,allTransfer);
		}
		return succ("ok", transferInfos);
	}
	
	/**
	 * 查询单个后台债权人
	 * @return
	 */
	@ActionKey("/queryTransferById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryTransferById(){
		//获取债权人编号
		String transferUserNo=getPara("transferUserNo", null);
		
		TransferInfo transferInfo = transferInfoService.findById(transferUserNo);
		
		//若信息有加密，此处要做解密操作
		
		return succ("查询单个后台债权人完成", transferInfo);
	}
	
	/**
	 * 修改后台债权人信息
	 * @return
	 */
	@ActionKey("/moTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message moTransfer(){
		//获取页面债权人信息
		//TransferInfo transferInfo=getModel(TransferInfo.class);
		String transferUserNo=getPara("transferUserNo");
		String name = getPara("name");
		String cardId=getPara("cardId");
		String address=getPara("address","");
		String mobile=getPara("mobile");
		String bankNo=getPara("bankNo");
		String bankUserName=getPara("bankUserName");
		String bankName=getPara("bankName");
		String area=getPara("area");
		String companyName=getPara("companyName","");
		String companyTel=getPara("companyTel","");
		String companyAddress=getPara("companyAddress","");
		String license=getPara("license","");
		String trusteeName=getPara("trusteeName");
		String trusteeCardId=getPara("trusteeCardId");
		String area2=getPara("area2","");
		
		Map<String, Object> para = new HashMap<String, Object>();
		para.put("name", name);
		para.put("cardId", cardId);
		para.put("address", address);
		para.put("mobile", mobile);
		para.put("bankNo", bankNo);
		para.put("bankUserName", bankUserName);
		para.put("bankName", bankName);
		para.put("area", area);
		para.put("companyName", companyName);
		para.put("companyAddress", companyAddress);
		para.put("license", license);
		para.put("trusteeName", trusteeName);
		para.put("companyTel", companyTel);
		para.put("trusteeCardId", trusteeCardId);
		para.put("area2", area2);
		
		boolean result = transferInfoService.modTransfer(transferUserNo,para);
		
		if(result){
			return succ("ok", result);
		}
		
		return error("no", "修改失败", null);
	}
	
	/**
	 * 添加新债权人信息
	 * @return
	 */
	@ActionKey("/addTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addTransfer(){
		TransferInfo transferInfo=TransferInfo.transferInfoDao;
		//获取页面债权人信息
		//TransferInfo transferInfo=getModel(TransferInfo.class);
		String name = getPara("name");
		String cardId=getPara("cardId");
		String address=getPara("address","");
		String mobile=getPara("mobile");
		String bankNo=getPara("bankNo");
		String bankUserName=getPara("bankUserName");
		String bankName=getPara("bankName");
		String area=getPara("area");
		String companyName=getPara("companyName","");
		String companyTel=getPara("companyTel","");
		String companyAddress=getPara("companyAddress","");
		String license=getPara("license","");
		String trusteeName=getPara("trusteeName");
		String trusteeCardId=getPara("trusteeCardId");
		String area2=getPara("area2","");
		
		String transferUserNo;//债权人code
		try {
			transferUserNo=CommonUtil.encryptUserCardId(cardId).substring(0, 10)+CommonUtil.encryptUserCardId(trusteeCardId).substring(0, 6);
		} catch (Exception e) {
			return error("01", "创建债权人时，信息加密过程发生错误:" + e.getMessage(), false);
		}
		
		transferInfo.put("transferUserNo",transferUserNo).put("name", name).put("cardId", cardId).put("address", address).put("mobile", mobile).put("bankNo", bankNo)
			.put("bankUserName", bankUserName).put("bankName", bankName).put("area", area).put("companyName", companyName)
			.put("companyAddress", companyAddress).put("license", license).put("trusteeName", trusteeName).put("companyTel", companyTel)
			.put("trusteeCardId", trusteeCardId).put("area2", area2);
		
		boolean result = transferInfoService.addTransfer(transferInfo);
		
		if(result){
			return succ("ok", result);
		}
		return error("no", "新增失败", null);
	}
	
	/**
	 * 删除债权人信息
	 * @return
	 */
	 
	@ActionKey("/deleteTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message deleteTransfer(){
		//获取债权人编码
		String transferUserNo=getPara("transferUserNo",null);
		
		boolean result = transferInfoService.deleteTransfer(transferUserNo);
		
		if(result){
			return succ("ok", result);
		}
		return error("no", "删除信息出错！", null);
		
	}
	
	/**
	 * 根据身份证号查询债权人信息
	 * @return
	 */
	@ActionKey("/queryTransferByCardId")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryTransferByCardId(){
		String cardId=getPara("cardId");
		List<TransferInfo> transferInfos = transferInfoService.queryTransferByCardId(cardId);
		if(transferInfos!=null&&transferInfos.size()!=0){
			return succ("ok", transferInfos.get(0));
		}else {
			return error("no", "未找到债权人！", null);
		}
		
	}
	
	
}
