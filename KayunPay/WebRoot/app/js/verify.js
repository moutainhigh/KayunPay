/**
 * 验证长度
 * @param that
 * @returns {Boolean}
 */
function lengthVerify(that){
	 var minextent=$(that).attr("minextent");//获取下限
	 var maxlength=$(that).attr("maxlength");//获取上限
	 var text=$(that).val();//获取输入框数据
	 if(minextent!=null&&text.length<minextent){//输入长度小于最小值且minextent不为空
			 return false;
	 }
	 if(maxlength!=null&&text.length>maxlength){//输入长度超过最大值且maxlength不为空
			 return false;
	 }
	 return true;//无上下限不验证,或验证通过
}