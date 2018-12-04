package com.dutiantech.config.routes;

import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.PublicController;
import com.dutiantech.controller.SecretController;
import com.dutiantech.controller.branch.YiStageController;
import com.dutiantech.controller.pay.FuiouPayController;
import com.dutiantech.controller.pay.JXPayController;
import com.dutiantech.controller.pay.SYXPayController;
import com.jfinal.config.Routes;

public class PublicRoutes extends Routes{

	@Override
	public void config() {
		add("pub_controal" , PublicController.class );
//		add("pay4lianlian" , LianLianPayController.class );
		add("pay4syx" , SYXPayController.class );
		add("fuiouController", FuiouController.class);
		add("fuiouPay", FuiouPayController.class);
		add("jxQueryController",JXQueryController.class);
		add("jxPayController", JXPayController.class);
		add("yfq",YiStageController.class);	// 易分期接口
		
		// 测试接口
		add("secret", SecretController.class);
	}
	
}
