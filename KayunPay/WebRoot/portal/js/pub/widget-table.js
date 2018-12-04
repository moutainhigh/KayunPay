(function(){
	
	function getMaxPage(total , size ){
		var pn = Math.floor(total / size );	
		if( total%size > 0 )
			pn += 1 ;
		return pn ;
	}
	/**
	 * index   = pageNumber 第几页 
	 * size    = pageSize  每页条数  
	 * total   = totalRow  总条数
	 * 
	 * */
	$.fn.pag = function( index , size , total , callback ){
		this.html("");	//clear
		var html = "<a index='#{index}' class='#{className}'>#{desc}</a> " ;
		
		var maxPageNum = getMaxPage( total , size );

		var outHtml = html.makeHtml( {
			index : 1 ,
			className : "" ,
			desc : "首页"
		} );	//首页
		
		outHtml += html.makeHtml( {
			index : (index <= maxPageNum)&& index > 1 ? index-1 : index ,
			className : "" ,
			desc : "上一页"
		} );	//首页
		
		outHtml += html.makeHtml( {
			index : (index+1)<=maxPageNum ? index+1 : maxPageNum ,
			className : "" ,
			desc : "下一页"
		} );	

		outHtml += html.makeHtml( {
			index : maxPageNum ,
			className : "" ,
			desc : "尾页"
		} );	
		
		outHtml += "&nbsp;&nbsp;<input class='goValue' size=3 style='height:15px;'/>";
		
		outHtml += html.makeHtml( {
			index : "" ,
			className : "" ,
			desc : "GO"
		} );
		
		if( maxPageNum > 0 ){
			outHtml += "<span style='margin-right:5px;'>第"+ index +"页</span>";
		}else{
			outHtml += "<span>第"+ 0 +"页</span>";
		}

		outHtml += "<span>共"+ maxPageNum+"页</span>";
				
		this.append( outHtml);
		
		var goThat=this.find("a:last");//GO栏点击按钮对象
		var goValue=this.find(".goValue");//GO栏输入框对象
		
		goValue.blur(function(){
			var indexGo=goValue.val();
			if(indexGo==null||isNaN(indexGo)||indexGo<1){
				indexGo=1;
			}
			var indexGo=parseInt(indexGo);//取整
			if(indexGo>maxPageNum){
				indexGo=maxPageNum;
			}
			goThat.attr("index",indexGo);
		});
		
		this.find("a[index!="+index+"]").click(callback);
		return this ;
	};
	
	//暂无数据
	$.fn.noData = function(){
	
		if( !( $(this).find("tfoot").length) ){
			$(this).find("tbody").html("");
			var tdNum = $(this).find("thead th").length;
			$(this).append("<tfoot><tr><td colspan="+ tdNum +" class='noData'>暂无数据</td></tr></tfoot>");
			
		}
	};
	
})();