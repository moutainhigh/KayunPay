(function(){
	/**
	*	计算每期等额本息的金额
	*	amount 总金额，单位分
	*	rate 年利率，比如24.01%，值为 2401
	*	limt 期限，单位月
	*/
	function dengebenxi( amount , rate , limit ){
		
		return benxi = (amount*rate)*Math.pow((1+rate) , limit ) / ( Math.pow( (1 + rate) , limit ) - 1 ) ;
		
	}

	/**
	*	按月付利
	*	
	*/
	function dengxi( amount , rate ){
		return amount*rate ;
	}
	
	/**
	 * 	理财计算工具
	 * 		amount 	总金额,单位分
	 * 		rate	利率,如利率为 24.13%,传入值则为 2413
	 * 		limit	周期，单位月
	 */
	YRHX.licai = function( amount , rate , limit  ){

		amount = parseFloat( amount );
		rate = parseFloat( rate/120000 ) ;
		
		this.denge = function( ta ){
			return dengebenxi( ta||amount , rate , limit );
		};
		
		this.dengxi = function( ta ){
			return dengxi( ta||amount , rate );
		};
		
		/**
		 * 
		 * 	获取某一个月需要付款的本息
		 */
		this.denge4month = function( monIndex ){
			var tmpAmount = amount ;
			var tmpInt = 0 ;
			for( var m = 1 ; i <= limit ; i ++ ){
				
			}
		};
		
		/**
		 * 	计算等额本息的明细
		 * 		返回 Array
		 */
		this.denge4year = function(){
			var tmpAmount = amount ;
			var tmpArr = [] ;
			var benxi = this.dengxi() ;	//第一个月利息
			var bx = this.denge( ) ;
			var startMon = 1 ;
			do{
				var xi = tmpAmount * rate ;
				tmpAmount = tmpAmount - bx + xi;
				tmpArr.push({
					benxi : bx ,
					ben : bx-xi ,
					xi : xi ,
					last : tmpAmount<0?0:tmpAmount
				});
				startMon ++ ;
			}while( startMon <= limit ) ;
			
			return tmpArr ;
		};
		
		return this ;
	};
	
	
})();