package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.Share;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class ShareService extends BaseService {

	
	private static final String basic_selectFields = " uid,userCode,userName,friendMobile,amount,regDateTime,regDate,comeFrom,remark ";

	/**
	 * 查询推荐列表
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<Share> queryShareList(Integer pageNumber, Integer pageSize, String userCode) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_share ";
		String sqlWhere = "where userCode = ?";
		String sqlOrder = " order by regDateTime desc ";
		return Share.shareDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere + sqlOrder, userCode);
	}
	
	/**
	 * 查询推荐列表   模糊查询
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<Share> queryShareListLike(Integer pageNumber, Integer pageSize, String option) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_share ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"userName","friendMobile"};
		makeExp4AnyLike(buff, paras, keys, option, "and","or");
		String sqlOrder = " order by uid desc ";
		return Share.shareDao.paginate(pageNumber, pageSize, sqlSelect,
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 统计个人总共邀请奖励
	 * @param userCode
	 * @return
	 */
	public Long sumShareAmount(String userCode){
		Record record = Db.findFirst(
				"select sum(amount) amount from t_share where userCode = ?", userCode);
		Object object = record.getColumns().get("amount");
		if(null == object){
			return 0L;
		}
		return Long.parseLong(object.toString());
	}

	/**
	 * 根据手机号查询推荐信息
	 * @param userMobile	加密后手机号
	 * @return
	 */
	public Share queryShareByMobile(String userMobile){
		String mobile = "";
		try {
			mobile = CommonUtil.decryptUserMobile(userMobile);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if(StringUtil.isBlank(mobile)){
			return null;
		}
		return Share.shareDao.findFirst("select * from t_share where friendMobile = ?",mobile);
	}
	
	
	
	
}











