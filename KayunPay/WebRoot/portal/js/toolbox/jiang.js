var jiangObj = {
	benjin : 0, //本金
	lilvY : 0, //年利率
	jiangli : 0, //奖励
	lixiM : 0,//利息管理费 百分比
	qixian : 0, //借款期限
	htype : 1,//还款方式
	show : true,//是否显示记录
	handle : function(){},//回调函数
	jiang : 0,//最终获得的奖励
	list : {}, //记录
	init : function(config){
		for(var i in config){
			if(i=="benjin") this.benjin = parseInt(config[i])*10000;
			if(i=="lilvY") this.lilvY = parseFloat(config[i]);
			if(i=="jiangli") this.jiangli = parseFloat(config[i]);
			if(i=="qixian") this.qixian = parseInt(config[i]);
			if(i=="lixiM") this.lixiM = parseInt(config[i])*0.01;
			if(i=="show") this.show = config[i];
			if(i=='htype') this.htype = config[i];
			if(i=='success') this.handle = config[i];
		}		
		this.record(config);
		this.getJiang();
		this.handle(this.list);		
	},
	record : function(config){
		config['success'] = function(obj){
			jiangObj.list = obj;
			//jiangObj.list.jiangli = jiangObj.jiang;
		};
		lixiobj.js(config);
	},
	getJiang : function(){
		var jiang = lixiobj.round(this.jiangli/12*100*this.qixian);
		switch(this.htype){
			case '1':				
			case '2':			
			case '3':
				this.list.jiangli = this.ay()+jiang;
				break;
			case '4':
				this.list.jiangli = this.bx()+jiang;
				break;
		}		
	},
	//按月等额本息; 按月付息,到期还本; 等额本金;
	ay : function(){
		var jiangli = 0;
		var lixiM = this.lixiM;//利息管理费
		var qixian = this.qixian;
		for(var i in this.list.record){
			var info = this.list.record[i];
			jiangli += info[2]*(1-lixiM);
		}
		return lixiobj.round(jiangli);
	},
	//到期还本息
	bx : function(){
		return lixiobj.round((this.list.total-this.benjin)*(1-this.lixiM));
	}
	
}
function checkjiang(){
	var jine = $("[name=jine]").val();
	var lilv = $("[name=lilv]").val();
	var jiangli = $("[name=jiangli]").val();
	var qixian = $("[name=qixian]").val();
	var lixi = $("[name=lixi]").val();
	var show = $("[name=showlist]").attr("checked");
	show = show=="checked" ? true : false;	
	var htype = 1;	
	$("[name=htype]").each(function(){		
		if($(this).attr("checked")=="checked"){
			htype = $(this).val();
		}
	});
	
	if(!/^[\d.]+$/.test(jine)){
		alert("借出金额不正确");
	}else if(!/^\d{1,2}(\.\d{1,2})?$/.test(lilv)){
		alert("年利率格式不正确");
	}else if(!/^\d{1,}$/.test(jiangli)){
		alert("奖励格式不正确");
	}else if(!/^\d{1,2}$/.test(qixian)){
		alert("借款期限格式不正确");
	}else if(!/^\d{1,}$/.test(lixi)){
		alert("利息管理费格式不正确");
	}else{
		jiangObj.init({
			benjin:jine,
			lilvY : lilv,
			jiangli : jiangli,
			qixian : qixian,
			lixiM : lixi,
			htype : htype,
			show : show,
			success : function(msg){				
				handle(msg);
				$("#jiangli").text(msg.jiangli);
			}
		})
	}
	return false;
}
