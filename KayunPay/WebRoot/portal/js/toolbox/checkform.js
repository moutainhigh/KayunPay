function checkjsq(){
	var oo = $("#dkjsq");
	var jine = oo.find('[name=jine]').val();
	var lilv = oo.find('[name=lilv]').val();
	var qixian = oo.find('[name=qixian]').val();
	var show = oo.find('[name=showlist]').attr('checked');
	show = show=='checked' ? true : false;	
	var htype;	
	oo.find('[name=htype]').each(function(){		
		if($(this).attr('checked')=='checked'){
			htype = $(this).val();
		}
	});
	
	if(!/^[\d.]+$/.test(jine)){
		alert('借款金额不正确');
	}else if(!/^\d{1,2}(\.\d{1,2})?$/.test(lilv)){
		alert('年利率格式不正确');
	}else if(!/^\d{1,2}$/.test(qixian)){
		alert('借款期限格式不正确');
	}else if(htype==undefined){
		alert("请选择还款方式");
	}else{	
		lixiobj.js({
			benjin:parseFloat(jine),
			lilvY:lilv,
			qixian:qixian,
			show:show,
			htype:htype,
			success : function(msg){					
						handle(msg);
					  }
		});	
	}
	
	return false;
}
function handle(msg){	
	$('#rtmsg1').show();
	if(msg.htype==1 || msg.htype==2){
		$('#bxl1').html(msg.htype==1 ? '每月需还本息' : '每月需还本金');
		$('#bx1').html(msg.htype==1 ? msg.hkM.toFixed(2) : msg.bjM.toFixed(2));
	}else if(msg.htype==3 || msg.htype==4){
		$('#bxl1').html(msg.htype==3 ? '每月付息' : '待还利息');
		$('#bx1').html(msg.rlixi.toFixed(2));
	}
	$('#qixian1').html(msg.qixian);
	$('#lilvm1').html((msg.lilvM*100).toFixed(2)+'%');
	$('#total1').html('￥'+msg.total.toFixed(2));							
	if(msg.show == true){						
		$('#rtmsg1 .record').show();
		$('#rtmsg1 .record .tt').remove();
		var rs = 0;
		for(var i in msg.record){							
			rs++;
			$('#rtmsg1 .record').append('<div class="tr tt"><div>'+rs+
										'月</div><div>'+msg.record[i][0].toFixed(2)+
										'</div><div>'+msg.record[i][1].toFixed(2)+
										'</div><div>'+msg.record[i][2].toFixed(2)+
										'</div><div>'+msg.record[i][3].toFixed(2)+
										'</div><div>'+msg.record[i][4].toFixed(2)+'</div></div>');
		}
	}else{
		$('#rtmsg1 .record').hide();
		$('#rtmsg1 .record .tt').remove();
	}	
}

function checkjiang(){
	var oo = $("#lcjsq");
	var jine = oo.find("[name=jine]").val();
	var lilv = oo.find("[name=lilv]").val();
	var jiangli = oo.find("[name=jiangli]").val();
	var qixian = oo.find("[name=qixian]").val();
	var lixi = oo.find("[name=lixi]").val();
	var show = oo.find("[name=showlist]").attr("checked");
	show = show=="checked" ? true : false;	
	var htype;	
	oo.find("[name=htype]").each(function(){		
		if($(this).attr("checked")=="checked"){
			htype = $(this).val();
		}
	});
	
	if(!/^[\d.]+$/.test(jine)){
		alert("借出金额不正确");
	}else if(!/^\d{1,2}(\.\d{1,2})?$/.test(lilv)){
		alert("年利率格式不正确");
	}else if(!/^\d{1,2}(\.\d{1,2})?$/.test(jiangli)){
		alert("奖励格式不正确");
	}else if(!/^\d{1,2}$/.test(qixian)){
		alert("借出期限格式不正确");
	}else if(!/^\d{1,}$/.test(lixi)){
		alert("利息管理费格式不正确");
	}else if(htype==undefined){
		alert("请选择还款方式");
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
				jhandle(msg);
				$("#jiangli").text(msg.jiangli);
			}
		})
	}
	return false;
}
function jhandle(msg){	
	var oo = $("#lcjsq");
	oo.find('#rtmsg').show();
	if(msg.htype==1 || msg.htype==2){
		oo.find('#bxl').html(msg.htype==1 ? '每月需收本息' : '每月需收本金');
		oo.find('#bx').html(msg.htype==1 ? msg.hkM.toFixed(2) : msg.bjM.toFixed(2));
	}else if(msg.htype==3 || msg.htype==4){
		oo.find('#bxl').html(msg.htype==3 ? '每月付息' : '待收利息');
		oo.find('#bx').html(msg.rlixi.toFixed(2));
	}
	oo.find('#qixian').html(msg.qixian);
	oo.find('#lilvm').html((msg.lilvM*100).toFixed(2)+'%');
	oo.find('#total').html('￥'+msg.total.toFixed(2));							
	if(msg.show == true){						
		oo.find('#rtmsg .record').show();
		oo.find('#rtmsg .record .tt').remove();
		var rs = 0;
		for(var i in msg.record){							
			rs++;
			oo.find('#rtmsg .record').append('<div class="tr tt"><div>'+rs+
										'月</div><div>'+msg.record[i][0].toFixed(2)+
										'</div><div>'+msg.record[i][1].toFixed(2)+
										'</div><div>'+msg.record[i][2].toFixed(2)+
										'</div><div>'+msg.record[i][3].toFixed(2)+
										'</div><div>'+msg.record[i][4].toFixed(2)+'</div></div>');
		}
	}else{
		oo.find('#rtmsg .record').hide();
		oo.find('#rtmsg .record .tt').remove();
	}	
}
$(".u-tab li").click(function(){
	var id = $(this).attr("val");
	$(this).siblings().removeClass("current");
	$(this).addClass("current");
	$(".u-form-wrap").hide();
	$("#"+id).show();
});
