package com.dutiantech.controller.admin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.dutiantech.Message;
import com.dutiantech.UploadResult;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.admin.validator.FilesUploadValidator;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Files;
import com.dutiantech.plugins.QCloudPic;
import com.dutiantech.service.FilesService;
import com.dutiantech.util.StringUtil;
import com.dutiantech.vo.UploadFile;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class FileController extends BaseController {
	
	private FilesService filesService = getService(FilesService.class);
	@Deprecated
	@ActionKey("/uploadFile")
	@Before({FilesUploadValidator.class,Tx.class,PkMsgInterceptor.class})
	public void uploadFile(){
		String type = getPara("fileType","C");
		String remark = getPara("fileRemark","");
		String userCode = getPara("userCode","");
		String opuserCode = "sys";
		String opUserName = "admin";
		Map<String,Object> fileInfo = uploadFile(type,remark , userCode, opuserCode, opUserName);
		if(fileInfo!=null){
			fileInfo.put("error", "0");
			//return succ("上传完成", fileInfo);
			renderJson(fileInfo);
		}else{
			fileInfo = new HashMap<String, Object>();
			fileInfo.put("error", "1");
		}
		renderJson(fileInfo);
		//return error("01", "上传失败", null);
	}
	
	/**
	 * 上传文件
	 * @param type 图片类型 ： A标     B身份证认证图片     C-网站内容    D视频     O其它
	 * @return
	 */
	private Map<String,Object> uploadFile(String type,String fileRemark,String userCode,String opUserCode,String opUserName){
		UploadFile file = UploadFile.makeFile(getRequest());
		UploadResult result = new UploadResult();
		int ret = QCloudPic.upload( file.getData() , result);
		if (ret == 0) {
			Map<String,Object> para = new HashMap<String, Object>();
			para.put("remoteCode", result.fileid);
			para.put("fileSize", file.getData().length);
			para.put("fileName", file.getName());
			para.put("fileSource", result.download_url);
			para.put("fileType", type);
			para.put("fileRemark",fileRemark);
			para.put("userCode", userCode);
			para.put("opUserCode", opUserCode);
			para.put("opUserName", opUserName);
			if(filesService.saveFiles(para)){
				para.remove("opUserCode");para.remove("opUserName");
				return para;
			}
		}
		return null;
	}
	
	@ActionKey("/getLoanPic")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanPic(){
		String loanCode = getPara("loanCode","");
		List<Files> list = filesService.findByLoanCode(loanCode);
		return succ("查询标的图片材料完成", list);
	}
	
	@ActionKey("/deleteFileById")
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message deleteById(){
		String remoteCode = getPara("remoteCode","");
		
		if(filesService.deleteById(remoteCode))
			return succ("删除单个文件属性信息完成", true);
		
		return error("00", "删除单个文件属性信息失败", false);
	}
	
	@ActionKey("/getFileById")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFileById(){
		
		String remoteCode = getPara("remoteCode","");
		
		Files file = filesService.findById(remoteCode);
		
		return succ("查询单个文件属性信息完成", file);
		
	}
	
	@ActionKey("/getFileByPage")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFileByPage(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDateTime = getPara("beginDate","");
		
		beginDateTime = StringUtil.isBlank(beginDateTime)?"":beginDateTime+"000000";
		
		String endDateTime = getPara("endDate","");
		
		endDateTime = StringUtil.isBlank(endDateTime)?"":endDateTime+"000000";

		Page<Files> pageFiles =  filesService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime);
		if(pageNumber > pageFiles.getTotalPage() && pageFiles.getTotalPage() > 0){
			pageNumber = pageFiles.getTotalPage();
			pageFiles = filesService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime);
		}
		return succ("分页查询文件属性信息完成", pageFiles);
	}
	
	
	
	@ActionKey("/downFileFromFtp")
	@Before({PkMsgInterceptor.class})
	public Message downFileFromFtp(){
		
//		String ftpHost = "sftp.credit2go.cn";//服务器地址
		String ftpHost = "sftp.jx-bank.com";//新的服务器地址
		String ftpUserName = "yironghengxin";//用户名
		int ftpPort = 5132;//端口号
		String ftpPassword = "vLUEC8CKa0EHi";//密码
		Map<String, Boolean> map = JXController.downFileFromJXFtp(ftpHost, ftpUserName, ftpPort, ftpPassword);
		
		return succ("操作成功", map);
				
	}

}
