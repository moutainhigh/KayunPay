package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.SliderPic;
import com.dutiantech.service.SliderPicService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

@Before(value=AuthInterceptor.class)
public class SliderPicController extends BaseController {

	private SliderPicService sliderService = getService(SliderPicService.class);

	@ActionKey("/addSlider")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addSlider() {
		SliderPic slider = getModel(SliderPic.class, "slider");
		if(slider == null){
			return error("01", "参数错误", "");
		}
		
		boolean save = sliderService.save(slider);
		if(save){
			return succ("ok", "" );
		}else{
			return error("02", "保存失败！", "");
		}
	}
	
	@ActionKey("/querySliderList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message querySliderList() {
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String isDisplay = getPara("isDisplay");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		
		Page<SliderPic> querySliderPic = sliderService.querySliderList(pageNumber , pageSize , isDisplay);
		
		return succ("查询成功", querySliderPic);
	}
	
	/**
	 * 查询公告新闻详细信息
	 */
	@ActionKey("/findSliderPic")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message findSliderPic(){
		String id = getPara("id");
		if(StringUtil.isBlank(id)){
			return error("01", "参数错误", "");
		}
		SliderPic slider = new SliderPic();
		slider = sliderService.findSlider(id);
		return succ("查询成功!", slider);
	}
	
	/**
	 * 删除
	 * @return
	 */
	@ActionKey("/delSliderPic")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message delSliderPic(){
		String id = getPara("id");
		if(StringUtil.isBlank(id)){
			return error("01", "参数错误", "");
		}
		boolean delSlider = sliderService.delSlider(id);
		if(delSlider == false){
			return error("02", "删除异常", "");
		}
		return succ("删除成功", null ) ;
	}
	
	@ActionKey("/updateSliderPic")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateSliderPic() {
		SliderPic slider = getModel(SliderPic.class, "slider");
		if(slider == null){
			return error("01", "参数错误", "");
		}
		boolean save = sliderService.updateSlider(slider);
		if(save){
			return succ("ok", "" );
		}else{
			return error("02", "修改失败！", "");
		}
	}
	
	
}





