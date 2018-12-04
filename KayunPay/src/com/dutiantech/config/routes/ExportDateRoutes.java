package com.dutiantech.config.routes;

import com.dutiantech.controller.export.DateInterfaceController;
import com.jfinal.config.Routes;

public class ExportDateRoutes extends Routes{

	@Override
	public void config() {
		add("wdzj_data", DateInterfaceController.class);
	}

	
}
