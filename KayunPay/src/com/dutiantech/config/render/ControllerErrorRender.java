package com.dutiantech.config.render;

import com.dutiantech.Log;
import com.jfinal.render.Render;

public class ControllerErrorRender extends Render{

	String forwardURL = "";
	
	public ControllerErrorRender(String url) {
		forwardURL = url ;
	}
	
	@Override
	public void render() {
		try {
			request.getRequestDispatcher(forwardURL).forward( request , response );
		} catch (Exception e) {
			Log.error("未找到跳转URL：" + forwardURL , e);
		} ;
	}
	
}
