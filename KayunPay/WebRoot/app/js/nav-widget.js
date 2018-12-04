(function(){
	var PARENT_OBJ_PRE = "PARENT-" ;
	
	var HTML_DEMO = {
		"PARENT-NAV-HTML" : "<li class='admin-parent' id='root-#{navCode}'><a class='am-cf am-collapsed' data-am-collapse='{target: \"#collapse-nav-#{navCode}\"}'><span class='am-icon-file'></span> #{navTitle} <span class='am-icon-angle-right am-fr am-margin-right'></span></a><ul name='#{navTitle}' class='am-list admin-sidebar-sub am-collapse' id='collapse-nav-#{navCode}'></ul></li>" ,
		"CHIND-NAV-HTML" : "<li><a href='#' onClick='window.jumpUrl(\"#{navUrl}\")' class='am-cf admin-child'><span class='#{navClass}'></span> #{navTitle} </span></a></li>" 
	};
	
	var nav = {} ;
	/**
	 * 	config Object
	 * 		->root	
	 */
	nav.regist = function( config ){
		nav.root = $( config["root"] );
	};
	
	window.jumpUrl = function( url ){
		if( url != "!{navUrl}")
			$("#opBody").attr("src",url);
	}
	
	nav.genHtml = function( key , obj ){
		var rowHtml = HTML_DEMO[ key ] ;
		for( var kName in obj ){
			var kVal = obj[ kName ] ;
			rowHtml = rowHtml.replaceAll( "#{" + kName + "}" , kVal ) ;
		}
		return rowHtml ;
	}; 
	
	/**
	 * 	navData Array<Object>
	 * 		Object.navCode			root-#{navCode} / collapse-nav-#{navCode}
	 * 		Object.navTitle			#{navTitle}
	 * 		Object.class			#{navClass}
	 */
	nav.genParentNavs = function( navData ){

		if( navData ){
			for( var rowIndex in navData ){
				var row = navData[ rowIndex ];
				nav.genParentNav( row ) ; 
			}
		}
	}

	nav.genParentNav = function( row ){
		if( row ){
			var navCode = row["navCode"] ;
			var navTitle = row["navTitle"] ;
			var rowHtml = nav.genHtml( "PARENT-NAV-HTML" , row );
			nav.root.append( rowHtml );
			nav[ PARENT_OBJ_PRE + navCode ] = $("#collapse-nav-" + navCode );	//cached
		}
		
	};
	
	/**
	 * 	row Object
	 * 		parentCode
	 * 		navCode 
	 * 		navTitle
	 * 
	 */
	nav.appendChild = function( row ){
		if( !row )
			return false ;
		var parentCode = row["parentCode"];
		var navCode = row["navCode"] ;
		var navTitle = row["navTitle"] ;
		var parentObj = nav[ PARENT_OBJ_PRE + parentCode ];
		var rowHtml = nav.genHtml( "CHIND-NAV-HTML" , row ) ;
		parentObj.append( rowHtml );
	}
	
	window.nav = nav ;
	
	nav.ready = function(){
		$(".admin-child").click(function(){
			var ele = $(this);
			var parentName = ele.parent().parent().attr("name") ;
			var curNavName = ele.text().trim() ;
			if( parentName ){
				var titleObj = $(".am-fl");
				titleObj.find("strong").text( parentName );
				titleObj.find("small").text( curNavName );
			}
		});
	};
	
	/*jQuery --*/
	$.fn.nav = function( opts ){
		var that = this ;
		//set root 
		nav.root = that ;
		//for...data
		
		nav.ready() ;
		return that ;
	};
	
})();
