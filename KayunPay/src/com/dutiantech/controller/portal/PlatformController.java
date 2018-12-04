package com.dutiantech.controller.portal;

import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class PlatformController extends BaseController {

	public static int INVALIDTIME = 1800 * 4; // 单位秒

	@ActionKey("/platform/bind")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void bind() {
		String source = getPara("source");
		setCookieByHttpOnly("platform", source, INVALIDTIME);
		renderJson(succ("jump", ""));
		return;
	}
}
