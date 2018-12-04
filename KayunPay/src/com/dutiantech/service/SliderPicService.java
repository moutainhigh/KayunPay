package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.SliderPic;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Page;

public class SliderPicService extends BaseService {

	private static final String basic_selectFields = " id,picName,picMaxUrl,picMinUrl,picMaxClickUrl,picMinClickUrl,sort,isDisplay,dateTime ";
	
	
	/**
	 * 保存
	 * @param slider
	 * slider.picName			图片名称
	 * slider.picMaxUrl			大图地址
	 * slider.picMinUrl			小图地址
	 * slider.picMaxClickUrl	大图点击跳转路径
	 * slider.picMinClickUrl	小图点击跳转路径
	 * slider.sort				排序
	 * slider.isDisplay			是否显示
	 * @return
	 */
	public boolean save(SliderPic slider){
		slider.set("dateTime", DateUtil.getNowDateTime());
		return slider.save();
	}

	/**
	 * 查询轮播图片列表
	 * @param pageNumber
	 * @param pageSize
	 * @param isDisplay
	 * @return
	 */
	public Page<SliderPic> querySliderList(Integer pageNumber, Integer pageSize,String isDisplay) {
		String sqlSelect = "select " + basic_selectFields;
		String sqlFrom = "from t_slider_pic ";
		String sqlWhere = "where 1 = 1";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "isDisplay", "=", isDisplay, "and");
		String sqlOrder = " order by isDisplay desc,sort asc";
		return SliderPic.sliderDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere+makeSql4WhereHasWhere(buff) + sqlOrder,ps.toArray());
	}
	
	
	/**
	 * 查询
	 * @param id
	 * @return
	 */
	public SliderPic findSlider(String id) {
		return SliderPic.sliderDao.findById(id);
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 */
	public boolean delSlider(String id){
		return SliderPic.sliderDao.deleteById(id);
	}
	
	/**
	 * 修改公告新闻列表
	 * @param slider
	 * slider.picName
	 * slider.picMaxUrl
	 * slider.picMinUrl
	 * slider.picMaxClickUrl
	 * slider.picMinClickUrl
	 * slider.sort
	 * slider.isDisplay
	 * @return
	 */
	public boolean updateSlider(SliderPic sliderPic){
		sliderPic.set("dateTime", DateUtil.getNowDateTime());
		return sliderPic.update();
	}
	
}




