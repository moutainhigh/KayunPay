package com.dutiantech.service;

import java.util.List;
import java.util.Map;

import com.dutiantech.model.Contracts;
import com.dutiantech.model.OPUserV2;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class ContractsService extends BaseService {
	
	/**
	 * 查询单条合同信息
	 * @param contractCode
	 * 			合同编号
	 * @return
	 */
	public Contracts findById(String contractCode) {
		return Contracts.contractsDao.findById(contractCode);
	}
	
	/**
	 * 分页查询合同列表信息
	 * @param pNum
	 * 			当前页码
	 * @param pSize
	 * 			每页显示的数量
	 * @return
	 */
	public Page<Contracts> findContractsByPage(Integer pNum,Integer pSize,String dimSearch){
		return Contracts.contractsDao.paginate(pNum, pSize, "select contractCode,uid,title,opCode,updateDateTime",
				"from t_contracts where title like ? order by addDateTime desc","%"+dimSearch+"%");
	}
	
	/**
	 * 修改合同信息
	 * @param contractCode
	 * 				合同编号
	 * @param para
	 * 			合同参数
	 * @return
	 */
	public boolean modContracts(String contractCode,Map<String, Object> para){
		Contracts contracts = Contracts.contractsDao.findById(contractCode);
		contracts._setAttrs(para);
		
		return contracts.update();
	}
	
	/**
	 * 添加合同信息
	 * @param contracts
	 * @return
	 */
	public boolean save(Contracts contracts){
		
		return contracts.save();
	}
	
	/**
	 * 删除合同信息
	 * @param contractCode
	 * 				合同编码
	 * @return
	 */
	public boolean delContractsById(String contractCode){
		
		return Contracts.contractsDao.deleteById(contractCode);
	}
	
	/**
	 * 查询后台操作员
	 * @param op_code
	 * 				操作员编码
	 * @return
	 */
	public String queryOpName(String op_code){
		List<OPUserV2> list = OPUserV2.OPUserV2Dao.find("select op_name from t_op_user_v2 where op_code = '"+op_code+"'");
		if(list.isEmpty()){
			return op_code;
		}else{
			return list.get(0).getStr("op_name");
		}
		
	}
	
	public long countByDate(String date) {
		String sql="select count(DISTINCT contractsNum) from t_contracts where DATE_FORMAT(addDateTime, '%Y%m%d') = ?";
		return Db.queryLong(sql, date);
	}
	
	/**
	 * 根据合同编号查询整套合同
	 * @param contractsNum
	 * @return
	 */
	public List<Contracts> findByContractNum(String contractsNum){
		return Contracts.contractsDao.find("select * from t_contracts where contractsNum=?",contractsNum);
		
	}
	
}
