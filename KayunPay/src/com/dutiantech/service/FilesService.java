package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.Files;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Page;

public class FilesService extends BaseService{
	
	private static final String basic_selectFields = " userCode,remoteCode,fileSize,createDateTime,opUserCode,opUserName,updateDateTime,fileType,fileName,fileSource,fileRemark ";
	
	/**
	 * 根据唯一标识获取文件属性信息
	 * @param remoteCode
	 * @return
	 */
	public Files findById(String remoteCode){
		return Files.filesDao.findByIdLoadColumns(remoteCode, basic_selectFields);
	}
	
	/**
	 * 根据唯一标识删除文件属性信息
	 * @param remoteCode
	 * @return
	 */
	public boolean deleteById(String remoteCode){
		return Files.filesDao.deleteById(remoteCode);
	}
	
	public List<Files> findByLoanCode(String loanCode){
		String sqlSelect = "select userCode,remoteCode,createDateTime,updateDateTime,fileSource";
		String sqlFrom = " from t_files where loanCode=?";
		String sqlOrder = " order by createDateTime,updateDateTime desc";
		return Files.filesDao.find(sqlSelect+sqlFrom+sqlOrder, loanCode);
	}
	
	/**
	 * 分页获取文件属性信息
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始日期时间
	 * @param endDateTime		结束日期时间
	 * @return
	 */
	public Page<Files> findByPage(Integer pageNumber, Integer pageSize, String beginDateTime, String endDateTime){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = "from t_files ";
		StringBuffer sqlWhere = new StringBuffer("");
		String sqlOrder = " order by createDateTime,updateDateTime desc";
		
		List<Object> paras = new ArrayList<Object>();
		if(!StringUtil.isBlank(beginDateTime) && !StringUtil.isBlank(endDateTime)){
			paras.add(beginDateTime);paras.add(beginDateTime);
			sqlWhere.append("where createDateTime >= ? and createDateTime <= ?");
		}
		
		if(paras.size()>0){
			Object[] zou = new Object[paras.size()];
			for (int i = 0; i < paras.size(); i++) {
				zou[i]=paras.get(i);
			}
			return Files.filesDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere.toString()+sqlOrder,zou);
		}else{
			return Files.filesDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere.toString()+sqlOrder);
		}
	}
	/**
	 * 文件上传后，文件属性信息保存到t_files
	 * @param para		Map，包含以下字段<br><br>
	 * userCode			用户编码<br>
	 * remoteCode		文件服务器上唯一标石<br>
	 * fileSize			文件大小<br>
	 * opUserCode		后台用户编码<br>
	 * opUserName		后台用户姓名<br>
	 * fileType			文件类型<br>
	 * fileName			文件名<br>
	 * fileSource		目标文件归属地<br>
	 * fileRemark		文件描述<br>
	 * @return
	 */
	public boolean saveFiles(Map<String,Object> para){
		String nowDateTime = DateUtil.getNowDateTime();
		Files file = new Files();
		file._setAttrs(para);
		file.set("createDateTime", nowDateTime);
		file.set("updateDateTime", nowDateTime);
		return file.save();
	}

}
