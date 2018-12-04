package com.dutiantech.service;

import java.util.List;
import java.util.Map;

import com.dutiantech.model.TransferUser;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class TransferUserService extends BaseService{
	//查询的字段
	private static final String basic_selectFields=" transferUserNo,name,cardId,address,mobile,bankNo,bankUserName,bankName,area,companyName,companyTel,companyAddress,license,trusteeName,trusteeCardId,area2,status,sex ";
	
		
	/**
	 * 查询所有债权人  (不带分页)
	 */
	public List<TransferUser> queryTransferList(){
		
		return TransferUser.transferUserDao.find("select transferUserNo,name,cardId,companyName,area from t_transfer_user");
	}
	/**
	 * 分页查询债权人信息
	 * @param pageNumber
	 * 				当前查询页码
	 * @param pageSize
	 * 				每一页显示的数量
	 * @return
	 */
	public Page<TransferUser> findByPage(Integer pageNumber,Integer pageSize,String allArea,String allTransfer){
		String sqlSelect="select transferUserNo,name,cardId,companyName,area,trusteeName";
		String sqlFrom=" from t_transfer_user where name like ? and area like ? ";
		return TransferUser.transferUserDao.paginate(pageNumber, pageSize,sqlSelect,sqlFrom,"%"+allTransfer+"%","%"+allArea+"%");
	}
	
	/**
	 * 查询单个后台债权人信息
	 * @return 
	 */
	public TransferUser findById(String transferUserNo){
		
		return TransferUser.transferUserDao.findByIdLoadColumns(transferUserNo, basic_selectFields);
	}
	
	/**
	 * 修改债权人信息
	 * @param transferInfo
	 * 				债权人
	 * @return
	 */
	public boolean modTransfer(String transferUserNo,Map<String, Object> para ){
		TransferUser info = TransferUser.transferUserDao.findById(transferUserNo);
		para.put("updateDateTime", DateUtil.getNowDateTime());
		info._setAttrs(para);
		return info.update();
	}
	
	/**
	 * 添加新债权人信息
	 * @param transferInfo
	 * 				债权人
	 * @return
	 */
	public boolean addTransfer(TransferUser transferUser){
		return transferUser.save();
	}
	
	public boolean save(Map<String, Object> para) {
		para.put("transferUserNo", UIDUtil.generate());
		para.put("addDateTime", DateUtil.getNowDateTime());
		para.put("updateDateTime", DateUtil.getNowDateTime());
		TransferUser transferUser = new TransferUser();
		transferUser._setAttrs(para);
		return transferUser.save();
	}
	
	/**
	 * 删除债权人信息
	 * @return
	 */
	public boolean deleteTransfer(String transferUserNo){
		
		return TransferUser.transferUserDao.deleteById(transferUserNo);
	}
	
	/**
	 * 根据身份证号、区域查询债权人信息
	 */
	public List<TransferUser> queryByCardIdArea(String cardId,String area){
		List<TransferUser> transferInfos = TransferUser.transferUserDao.find("select transferUserNo,name from t_transfer_user where cardId=? and area=?",cardId,area);
		return transferInfos;
	}
}
