package com.dutiantech.service;

import java.util.List;
import java.util.Map;

import com.dutiantech.model.TransferInfo;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class TransferInfoService extends BaseService{
	//查询的字段
	private static final String basic_selectFields=" transferUserNo,name,cardId,address,mobile,bankNo,bankUserName,bankName,area,companyName,companyTel,companyAddress,license,trusteeName,trusteeCardId,area2 ";
	
		
	/**
	 * 查询所有债权人  (不带分页)
	 */
	public List<TransferInfo> queryTransferList(){
		
		return TransferInfo.transferInfoDao.find("select transferUserNo,name,cardId,companyName,area from t_transfer_info");
	}
	/**
	 * 分页查询债权人信息
	 * @param pageNumber
	 * 				当前查询页码
	 * @param pageSize
	 * 				每一页显示的数量
	 * @return
	 */
	public Page<TransferInfo> findByPage(Integer pageNumber,Integer pageSize,String allArea,String allTransfer){
		String sqlSelect="select transferUserNo,name,cardId,"+"if(companyName is null or companyName='',' ',companyName) companyName,"+"if(area is null or area='',' ',area) area,"+"if(trusteeName is null or trusteeName='',' ',trusteeName) trusteeName";
		return TransferInfo.transferInfoDao.paginate(pageNumber, pageSize,sqlSelect," from t_transfer_info where name like ? and area like ? ","%"+allTransfer+"%","%"+allArea+"%");
	}
	
	/**
	 * 查询单个后台债权人信息
	 * @return 
	 */
	public TransferInfo findById(String transferUserNo){
		
		return TransferInfo.transferInfoDao.findByIdLoadColumns(transferUserNo, basic_selectFields);
	}
	
	/**
	 * 修改债权人信息
	 * @param transferInfo
	 * 				债权人
	 * @return
	 */
	public boolean modTransfer(String transferUserNo,Map<String, Object> para ){
		TransferInfo info = TransferInfo.transferInfoDao.findById(transferUserNo);
		info._setAttrs(para);
		return info.update();
	}
	
	/**
	 * 添加新债权人信息
	 * @param transferInfo
	 * 				债权人
	 * @return
	 */
	public boolean addTransfer(TransferInfo transferInfo){
		return transferInfo.save();
	}
	
	public boolean save(TransferInfo transferInfo) {
		transferInfo.set("transferUserNo", UIDUtil.generate());
		transferInfo.set("addDateTime", DateUtil.getNowDateTime());
		transferInfo.set("updateDateTime", DateUtil.getNowDateTime());
		return TransferInfo.transferInfoDao.save();
	}
	
	/**
	 * 删除债权人信息
	 * @return
	 */
	public boolean deleteTransfer(String transferUserNo){
		
		return TransferInfo.transferInfoDao.deleteById(transferUserNo);
	}
	
	/**
	 * 根据身份证号查询债权人信息
	 */
	public List<TransferInfo> queryTransferByCardId(String cardId){
		List<TransferInfo> transferInfos = TransferInfo.transferInfoDao.find("select name from t_transfer_info where cardId=?",cardId);
		return transferInfos;
	}
}
