var lixiobj = {
	benjin:0,//贷款本金
	ms 	: 0,//剩余贷款
	total : 0,//还款总额
	lilvY : 0,//年利率
	lilvM : 0,//月利率
	lilvD : 0,//日利率
	qixian: 12,//还款期限(月)
	days	:	0,//还款天数(只针对天标)
	bjM	: 0,//每月还款本金
	other : 0,//其它管理费用
	hkM : 0,//每月还款本息
	htype : 1,//1,等额本息,2,等额本金
	show : true,//是否显示记录
	record : new Array(), //存放每月还款的明细记录
	handle : function(){},//回调函数
	rlixi : 0, //月还利息
	
	init : function(config){
		for(var i in config){
			if(i=='benjin') this.benjin = config[i]*10000;
			if(i=='lilvY') this.lilvY = config[i]*0.01;
			if(i=='qixian') this.qixian = config[i];
			if(i=='show') this.show = config[i];
			if(i=='htype') this.htype = config[i];
			if(i=='days') this.days = config[i];
			if(i=='success') this.handle = config[i];
		}
		this.lilvM = this.lilvY/12;
		this.lilvD = this.lilvM/30;
		this.ms = this.benjin;
		this.bjM = Math.round(this.benjin/this.qixian*100)/100;		
		this.total = this.benjin;
		this.record = new Array();
		this.other = this.benjin*0.5*0.01;
	},
	js : function(config){
		this.init(config);	
		if(this.days>0){
			this.dayFunc();
		}else{
			switch(this.htype){
				case '1':
					this.debx();
					break;
				case '2':
					this.debj();
					break;
				case '3':
					this.fx();
					break;
				case '4':
					this.bx();
					break;
			}
		}
		this.getHtml();
	},
	dayFunc	:	function(){
		var lixi = this.round(this.ms*this.lilvD*this.days);
		this.list(new Array(this.benjin+lixi,this.benjin,lixi,this.other,0));
	},
	//到期还本息
	bx	: function(){
		var lixi=0;
		lixi = this.round(this.ms*this.lilvM)*this.qixian;
		this.ms +=lixi;						
		lixi = this.round(this.ms-this.benjin);
		benxi = this.round(this.ms);		
		this.total += lixi;			
		this.ms = 0;
		this.rlixi = lixi;
        this.other *= this.qixian;
		this.list(new Array(benxi,this.benjin,lixi,this.other,this.ms));
	},
	//按月付息,到期还本
	fx	: function(){
		for(var i=0; i<this.qixian; i++){
			var lixi = this.round(this.ms*this.lilvM);			
			benxi = lixi;
			bjm = 0;
			if(i == this.qixian-1){				
				bjm = this.ms;
				benxi += this.ms;
				this.ms = 0;
			}
			this.rlixi = lixi;
			this.total += lixi;			
			this.list(new Array(benxi,bjm,lixi,this.other,this.ms));
		}
	},
	//等额本金
	debj : function(){
		for(var i=0; i<this.qixian; i++){
			var lixi = this.round(this.ms*this.lilvM);
			this.hkM = benxi = this.bjM+lixi;
			this.ms = this.ms-this.bjM;
			if(i == this.qixian-1){				
				benxi += this.ms;
				this.ms = 0;
			}
			this.total += lixi;			
			this.list(new Array(benxi,this.bjM,lixi,this.other,this.ms));
			//alert(re);
		}
	},
	//等额本息
	debx : function(){
		for(var i=0; i<this.qixian; i++){
			hk = (this.benjin*this.lilvM*Math.pow(1+this.lilvM,this.qixian))/(Math.pow(1+this.lilvM,this.qixian)-1);
			this.hkM = hk = this.round(hk);			
			var lixi = this.round(this.ms*this.lilvM);
			bjM = hk-lixi;
			this.ms = this.round(this.ms-bjM);
			if(i == this.qixian-1){
				bjM+=this.ms;
				hk+=this.ms;
				this.ms=0;
			}	
			bjM = this.round(bjM);
			this.total += lixi;
			this.list(new Array(this.hkM,bjM,lixi,this.other,this.ms));
		}		
	},
	list : function(cord){
		this.record.push(cord);
	},
	round : function(val,n){
		n = n==undefined ? 2 : n;
		var j = Math.pow(10,n);
		return Math.round(val*j)/j;
	},
	getHtml : function(){
		this.handle({
			benjin:this.benjin,
			bjM : this.bjM,
			qixian : this.qixian,
			lilvM : this.lilvM,
			total : this.total,
			hkM : this.hkM,
			bjM : this.bjM,
			record : this.record,
			show: this.show,
			htype : this.htype,
			rlixi : this.rlixi
		});
	}
}
var licaiObj = {
    benjin: 0, //贷款本金
    ms: 0, //剩余贷款
    total: 0, //还款总额
    cunlilvM: 0, //月纯利率
    lilvY: 0, //年利率
    lilvM: 0, //月利率
    lilvD: 0, //日利率
    qixian: 12, //还款期限(月)
    bjM: 0, //每月还款本金
    other: 0, //其它管理费用
    jiangli: 0, //奖励（百分比）
    jiangM: 0, //月奖励(元),
    jiangTotal: 0, //所有奖励
    hkM: 0, //每月还款本息
    htype: 1, //1,等额本息,2,等额本金
    show: true, //是否显示记录
    record: new Array(), //存放每月还款的明细记录
    handle: function () { }, //回调函数
    rlixi: 0, //月还利息

    init: function (config) {
        for (var i in config) {
            if (i == 'benjin') this.benjin = config[i] * 10000;
            if (i == 'lilvY') this.lilvY = config[i] * 0.01;
            if (i == 'qixian') this.qixian = config[i];
            if (i == 'show') this.show = config[i];
            if (i == 'htype') this.htype = config[i];
            if (i == 'success') this.handle = config[i];
            if (i == "other") this.other = config[i];
            if (i == "jiangli") this.jiangli = config[i] * 0.01;
        }
        this.cunlilvM = (this.lilvY) / 12;
        this.lilvM = (this.lilvY + this.jiangli) / 12;
        this.lilvD = this.lilvY / 365;
        this.ms = this.benjin;
        this.bjM = Math.round(this.benjin / this.qixian * 100) / 100;
        this.total = this.benjin;
        this.record = new Array();
        this.jiangM = 0; //this.benjin*this.jiangli/12;
        this.jiangTotal = 0;
        //this.other = this.benjin*0.5*0.01;
    },
    js: function (config) {
        this.init(config);
        switch (this.htype) {
            case '1':
                this.debx();
                break;
            case '2':
                this.debj();
                break;
            case '3':
                this.fx();
                break;
            case '4':
                this.bx();
                break;
        }
        this.getHtml();
    },
    //到期还本息
    bx: function () {
        var lixi = 0;
        lixi = this.round(this.ms * this.lilvM) * this.qixian;
        var other = this.round(this.ms * this.cunlilvM) * this.other * this.qixian;
        this.ms += lixi;
        lixi = this.round(this.ms - this.benjin);
        benxi = this.round(this.ms);
        this.total += lixi;
        this.ms = 0;
        this.rlixi = lixi;
        //this.other *= this.qixian;
        jiang = lixi - other;
        this.jiangTotal += jiang;
        this.list(new Array(benxi, this.benjin, lixi, jiang, this.ms));
    },
    //按月付息,到期还本
    fx: function () {
        for (var i = 0; i < this.qixian; i++) {
            var lixi = this.round(this.ms * this.lilvM);
            var other = this.round(this.ms * this.cunlilvM) * this.other;
            benxi = lixi;
            bjm = 0;
            if (i == this.qixian - 1) {
                bjm = this.ms;
                benxi += this.ms;
                this.ms = 0;
            }
            this.rlixi = lixi;
            this.total += lixi;
            jiang = lixi - other;
            this.jiangTotal += jiang;
            this.list(new Array(benxi, bjm, lixi, jiang, this.ms));
        }
    },
    //等额本金
    debj: function () {
        for (var i = 0; i < this.qixian; i++) {
            var lixi = this.round(this.ms * this.lilvM);
            var other = this.round(this.ms * this.cunlilvM) * this.other;
            this.hkM = benxi = this.bjM + lixi;
            this.ms = this.ms - this.bjM;
            if (i == this.qixian - 1) {
                benxi += this.ms;
                this.ms = 0;
            }
            this.total += lixi;
            jiang = lixi - other;
            this.jiangTotal += jiang;
            this.list(new Array(benxi, this.bjM, lixi, jiang, this.ms));
            //alert(re);
        }
    },
    //按月等额本息
    debx: function () {
        for (var i = 0; i < this.qixian; i++) {
            hk = (this.benjin * this.lilvM * Math.pow(1 + this.lilvM, this.qixian)) / (Math.pow(1 + this.lilvM, this.qixian) - 1);
            this.hkM = hk = this.round(hk);
            var lixi = this.round(this.ms * this.lilvM);
            var other = this.round(this.ms * this.cunlilvM) * this.other;
            bjM = hk - lixi;
            this.ms = this.round(this.ms - bjM);
            if (i == this.qixian - 1) {
                bjM += this.ms;
                hk += this.ms;
                this.ms = 0;
            }
            bjM = this.round(bjM);
            this.total += lixi;
            jiang = lixi - other;
            this.jiangTotal += jiang;
            this.list(new Array(this.hkM, bjM, lixi, jiang, this.ms));
        }
    },
    list: function (cord) {
        this.record.push(cord);
    },
    round: function (val, n) {
        n = n == undefined ? 2 : n;
        var j = Math.pow(10, n);
        return Math.round(val * j) / j;
    },
    getHtml: function () {
        this.handle({
            benjin: this.benjin,
            bjM: this.bjM,
            qixian: this.qixian,
            lilvM: this.lilvM,
            total: this.total,
            hkM: this.hkM,
            bjM: this.bjM,
            record: this.record,
            show: this.show,
            htype: this.htype,
            rlixi: this.rlixi,
            jiangli: this.round(this.jiangTotal)
        });
    }
};
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
		this.handle(this.list);		
	},
	record : function(config){
		config["jiang"] = true;
		config["other"] = this.lixiM;
		config['success'] = function(obj){
			jiangObj.list = obj;
			//jiangObj.list.jiangli = jiangObj.jiang;
		};
		licaiObj.js(config);
	}	
}
