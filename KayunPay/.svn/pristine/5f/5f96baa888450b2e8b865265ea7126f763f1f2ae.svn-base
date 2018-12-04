package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.OPUserV2;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class OPUserV2Service extends BaseService{
	
	private static final String basic_selectFields = " op_code,op_mobile,op_name,op_map,op_group,op_status,update_datetime,update_op_code,update_op_name,isBranch,branchArea,creditorName,creditorCardId,transferUserNo ";
	
	/**
	 * 查询已存在的手机号有几条数据
	 * @param opMobile	密文
	 * @return
	 */
	public long countByMobile(String opMobile){
		return Db.queryLong("select count(op_code) from t_op_user_v2 where op_mobile = ? ", opMobile);
	}
	
	public OPUserV2 findById(String userCode){
		return OPUserV2.OPUserV2Dao.findByIdLoadColumns(userCode, basic_selectFields);
	}
	
	public String findUserNameById(String userCode){
		return OPUserV2.OPUserV2Dao.findByIdLoadColumns(userCode, "op_name").getStr("op_name");
	}
	
	public boolean deleteById(String userCode){
		return OPUserV2.OPUserV2Dao.deleteById(userCode);
	}
	
	public OPUserV2 doLogin(String userName , String userPwd ){
		return OPUserV2.OPUserV2Dao.findFirst("select op_code,op_map,op_group,op_name" 
				+ " from t_op_user_v2 where op_mobile=? and op_pwd=? and op_status='A'", userName , userPwd ) ;
	}
	
	/**
	 * 手机号查询用户
	 * @param mobile
	 * @return
	 */
	public OPUserV2 query4mobile(String mobile){
		try {
			String userMobile = CommonUtil.encryptUserMobile(mobile);
			return OPUserV2.OPUserV2Dao.findFirst("select op_code,op_map,op_group,op_name" 
					+ " from t_op_user_v2 where op_mobile=? and op_status='A'", userMobile) ;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * 修改用户昵称
	 * @param opCode		后台用户编码
	 * @param op_name		新昵称
	 * @return
	 */
	public boolean updateUserName(String opCode,String op_name){
		OPUserV2 opUserV2 = OPUserV2.OPUserV2Dao.findByIdLoadColumns(opCode, "op_code,op_name");
		opUserV2.set("op_name", op_name);
		return opUserV2.update();
	}
	/**
	 * 修改密码
	 * @param opCode		后台用户编码
	 * @param newPasswd		新密码
	 * @return
	 */
	public boolean updatePassword(String opCode,String newPasswd){
		OPUserV2 opUserV2 = OPUserV2.OPUserV2Dao.findByIdLoadColumns(opCode, "op_code,op_pwd");
		opUserV2.set("op_pwd", newPasswd);
		return opUserV2.update();
	}
	
	/**
	 * 更新后台用户
	 * @param userCode		更新的OPUserV2的唯一标识用户编码
	 * @param newPasswd		密文新密码，null不修改
	 * @param para			Map参数，包含以下字段:<br><br>
	 * userName	
	 * roleMap				
	 * userGroup
	 * roleName
	 * updateUserCode
	 * updateUserName
	 * userLevel
	 * userState
	 * @return
	 */
	public boolean updateById(String userCode,String newPasswd,Map<String,Object> para){
		String nowDateTime = DateUtil.getNowDateTime();
		OPUserV2 opUserV2 = OPUserV2.OPUserV2Dao.findById(userCode);
		opUserV2._setAttrs(para);
		opUserV2.set("update_datetime", nowDateTime);
		if(!StringUtil.isBlank(newPasswd)){
			opUserV2.set("op_pwd", newPasswd);
		}
		return opUserV2.update();
	}
	
	/**
	 * 添加一个后台用户
	 * 
	 * @param para	Map参数，包含以下字段：<br><br>
	 * userMobile		密文手机号<br>
	 * userName			昵称<br>
	 * userPwd			密文密码<br>
	 * roleMap			<br>
	 * userGroup		<br>
	 * roleName			<br>
	 * updateUserCode	<br>
	 * updateUserName	<br>
	 * userLevel		<br>
	 * userState		<br>
	 * @return
	 */
	public boolean save(Map<String, Object> para){
		OPUserV2 opUserV2 = new OPUserV2();
		opUserV2._setAttrs(para);
		String nowDateTime = DateUtil.getNowDateTime() ;
		opUserV2.set("create_datetime", nowDateTime);
		opUserV2.set("update_datetime", nowDateTime);
		opUserV2.set("op_code", UIDUtil.generate());
		return opUserV2.save();
	}
	
	
	/**
	 * 分页获取后台用户
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始日期时间
	 * @param endDateTime		结束日期时间
	 * @return
	 */
	public Page<OPUserV2> findByPage(Integer pageNumber, Integer pageSize, String beginDateTime, String endDateTime , String allkey){
		
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_op_user_v2 ";
		String sqlOrder = " order by create_datetime desc,update_datetime desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"op_name"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		makeExp(buff, ps, "update_datetime", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "update_datetime", "<=" , endDateTime , "and");
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, ps, "op_mobile", "=" , x , "or");
		}
		return OPUserV2.OPUserV2Dao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
	}
	
}