package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.Notice;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Page;


public class NoticeService extends BaseService {
	
	private static final String basic_selectFields = " id,title,description,content,url,pic,type,keyWords,clicks,addDateTime,upDateTime ";
	private static final String min_selectFields = " id,title,description,url,pic,clicks,type,addDateTime,upDateTime ";

	/**
	 * 查询公告新闻列表
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @param isContent
	 * @return
	 */
	public Page<Notice> queryNotice(Integer pageNumber, Integer pageSize, String type,
			String isContent) {
		String sqlSelect = "select ";
		if("0".equals(isContent)){
			sqlSelect = sqlSelect + basic_selectFields;
		}if("2".equals(isContent)){
			sqlSelect = sqlSelect + " id nid,title,ifnull(url,'') url,pic,type,description,addDateTime,upDateTime ";
		}else{
			sqlSelect = sqlSelect + min_selectFields;
		}
		String sqlFrom = "from t_notice ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "status", "=", "1", "and");
		if(!StringUtil.isBlank(type)){
			makeExp(buff, ps, "type", "=", type, "and");
		}
		String sqlOrder = " order by id desc ";
		return Notice.noticeDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + makeSql4Where(buff) + sqlOrder,ps.toArray());
	}
	
	/**
	 * 查询app公告新闻活动列表
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @param isContent
	 * @return
	 */
	public Page<Notice> queryNotice4App(Integer pageNumber, Integer pageSize, String type, String title) {
		String sqlSelect = "select  id nid,title,ifnull(url,'') url,pic,type,description,addDateTime,upDateTime ";
		String sqlFrom = "from t_notice ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "status", "=", "1", "and");
		if(!StringUtil.isBlank(type)){
			makeExp(buff, ps, "type", "=", type, "and");
		}
		if(!StringUtil.isBlank(title)){
			makeExp(buff, ps, "title", "like", title, "and");
		}
		String sqlOrder = " order by id desc ";
		return Notice.noticeDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + makeSql4Where(buff) + sqlOrder,ps.toArray());
	}
	
	/**
	 * 查询
	 * @param id
	 * @return
	 */
	public Notice queryNewsDetail(String id) {
		return Notice.noticeDao.findById(id);
	}
	
	/**
	 * 新增公告新闻列表
	 * @param title				标题
	 * @param description		描述
	 * @param content			内容
	 * @param url				外链地址(特殊类别 如媒体报道  才使用此字段）
	 * @param pic				标题图片路径
	 * @param type				0最新公告  1公司新闻 2行业动态  3媒体报道
	 * @param keyWords			关键词 查询用
	 * @return
	 */
	public boolean saveNotice(Notice notice){
		notice.set("clicks", 0);
		notice.set("status", 1);
		notice.set("addDateTime", DateUtil.getNowDateTime());
		notice.set("upDateTime", DateUtil.getNowDateTime());
		return notice.save();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public boolean delNotice(String id){
		return Notice.noticeDao.deleteById(id);
	}
	
	
	/**
	 * 修改公告新闻列表
	 * @param title				标题
	 * @param description		描述
	 * @param content			内容
	 * @param url				外链地址(特殊类别 如媒体报道  才使用此字段）
	 * @param pic				标题图片路径
	 * @param type				0最新公告  1公司新闻 2行业动态  3媒体报道
	 * @param keyWords			关键词 查询用
	 * @return
	 */
	public boolean updateNotice(Notice notice){
		notice.remove("clicks");
		notice.set("upDateTime", DateUtil.getNowDateTime());
		return notice.update();
	}

}







