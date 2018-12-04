package com.dutiantech.interceptor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.controller.BaseController;
import com.dutiantech.service.YistageTraceService;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.HttpKit;
import com.jfinal.render.JsonRender;
import com.jfinal.render.NullRender;
import com.jfinal.render.Render;
import com.jx.util.DateUtil;

public class YistageInterceptor extends BaseController implements Interceptor {

	private YistageTraceService yistageTraceService = getService(YistageTraceService.class);

	@Override
	public void intercept(Invocation inv) {
		Controller controller =  inv.getController();
		HttpServletRequest request = controller.getRequest();
		String method = request.getMethod();//请求的方式
		String actionKey = inv.getActionKey();// 获取访问的接口名
//		String traceCode = UIDUtil.generate();// 生成traceCode
		String txDate = DateUtil.getDate();
		String txTime = DateUtil.getTime();
		String seqNo = DateUtil.getRandomStr(6);
		String traceCode = txDate+txTime+seqNo;
		String readData = "";//请求的数据
		//如果是GET请求，将参数转化成json格式，更新请求数据
		if("GET".equals(method)){
			JSONObject jso = new JSONObject();
			Map<String, String[]> parameterMap = request.getParameterMap();
			Set<String> keys = parameterMap.keySet();
			Iterator<String> iter = keys.iterator() ;
			while(iter.hasNext()){
				String key = iter.next();
				String parameter = request.getParameter(key);
				jso.put(key, parameter);
			}
			readData = jso.toJSONString();
		}else{
			readData = HttpKit.readData(request);
		}
	
		yistageTraceService.save(traceCode, actionKey,readData);
		request.setAttribute("params", readData);
		request.setAttribute("traceCodeForYfq", traceCode);

		inv.invoke();

		// 获取返回的json字符串
		Render render2 = inv.getController().getRender();
		if (render2 != null &&!(render2 instanceof  NullRender)) {
			String jsonStr = ((JsonRender) inv.getController().getRender()).getJsonText();
			yistageTraceService.updateResponseMessage(traceCode, jsonStr);
		}
		
	}

}
