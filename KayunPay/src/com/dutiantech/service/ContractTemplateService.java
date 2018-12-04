package com.dutiantech.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.ContractTemplate;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class ContractTemplateService extends BaseService {
	
	private static final String BASE_FIELD = "templateCode, uid, title, content, status, addDateTime, updateDateTime";
	
	//通过模板Code查询合同模板
	public ContractTemplate findById(String templateCode) {
		return ContractTemplate.contractTemplateDao.findById(templateCode);
	}
	
	public Map<String, ContractTemplate> findAll() {
		List<ContractTemplate> lstContractTemplates = ContractTemplate.contractTemplateDao.find("SELECT " + BASE_FIELD + " FROM t_contract_template");
		Map<String, ContractTemplate> result = new HashMap<String, ContractTemplate>();
		for (ContractTemplate contractTemplate : lstContractTemplates) {
			result.put(contractTemplate.getStr("title"), contractTemplate);
		}
		return result;
	}

	//查询合同模板列表
	public Page<ContractTemplate> findAllByPage(Integer pageNumber, Integer pageSize, String allkey) {

			String sqlSelect = "select * ";
			String sqlFrom = " from t_contract_template ";
			String sqlOrder = "";
			StringBuffer buff = new StringBuffer("");
			List<Object> ps = new ArrayList<Object>();
			String[] keys = new String[]{"title"};
			makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
			
			if(!StringUtil.isBlank(allkey)){
				if(allkey.equals("可用")){
					allkey="A";
				}if(allkey.equals("禁用")){
					allkey="B";
				}
				makeExp(buff,ps,"status","=",allkey,"or");
			}
			Page<ContractTemplate> templatePage = ContractTemplate.contractTemplateDao.paginate(pageNumber,pageSize,sqlSelect,sqlFrom+makeSql4Where(buff)+sqlOrder,ps.toArray());
			return templatePage;

	}
	
	//添加合同模板
	public boolean saveTemplate(ContractTemplate contractTemplate){
		contractTemplate.put("templateCode",UIDUtil.generate());
		contractTemplate.put("addDateTime",DateUtil.getDateStrFromDateTime(new Date()));
		contractTemplate.put("updateDateTime",DateUtil.getDateStrFromDateTime(new Date()));
		
		return contractTemplate.save();
		
	}
	
	//删除合同模板
	public boolean deleteByCode(String templateCode){
		return ContractTemplate.contractTemplateDao.deleteById(templateCode);
	}
	
	//修改模板
	public boolean changeByCode(ContractTemplate contractTemplate){
		return contractTemplate.update();
	}

	//禁用或解禁合同模板
	public boolean updateByCode(String[] templateCode, String status) {
		for(int i=0;i<templateCode.length;i++){
			ContractTemplate contractTemplate = ContractTemplate.contractTemplateDao.findByIdLoadColumns(templateCode[i], "templateCode,status");
			contractTemplate.set("status",status);
			boolean updates = contractTemplate.update();
			if(updates==false){
				return false;
			}
		}
		return true;
		
	}

	
	
}
