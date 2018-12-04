package com.dutiantech;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.dutiantech.anno.AuthNum;
import com.jfinal.core.ActionKey;

/**
 * 	扫描
 * @author wucheng
 *
 */
public class ScanMain {

	private static String packageRoot = "com.dutiantech";	//包的根节点
	@SuppressWarnings("rawtypes")
	private static Class annoClass = AuthNum.class ;
	
	private static Class<?> getClass(String className) throws ClassNotFoundException{
		return Class.forName(packageRoot + ".controller.admin." + className );
	}
	
	@SuppressWarnings("unchecked")
	private static void getRoleValue(Class<?> cls){
		//AuthNum auth = cls.getAnnotation(annoClass) ;
		Method[] methods = cls.getDeclaredMethods() ;
		for( Method method : methods){
			Annotation an = method.getAnnotation( annoClass ) ;
			if( an != null ){
				AuthNum auth = (AuthNum) an ;
				//TODO 如何处理
//				System.out.println(method.getName() + " "  + auth.value() );
				doAnno( cls , method , auth );
			}
		}
		
	}
	private static Map<String , Integer > checkMap = new HashMap<String , Integer>();
	private static void getControllerNames() throws ClassNotFoundException{
		String path = (ScanMain.class.getResource("").toString() + "controller/admin/").replace("file:", "");
		File rootPath = new File(path) ;
		File[] files = rootPath.listFiles();
		for(File file : files ){
			String fileName = file.getName();
			if(fileName.indexOf(".class")!=-1){
				fileName = fileName.replace(".class", "");
				Class<?> cls = getClass(fileName);
//				if( fileName.equals("OPUserV2Controller"))
					getRoleValue( cls );
//					if( cls.getSuperclass() == BaseController.class ){
//						System.out.println( getRoleValue(cls.getDeclaredMethods().getClass()) );
//					}
			}
		}
	}

	private static void doAnno(Class<?> cls , Method m , AuthNum auth ){
		ActionKey action = m.getAnnotation(ActionKey.class) ;
		String url = "";
		if( action != null ){
			url = action.value() ;
		}
		//TODO do something
		int v = auth.value() ;
		if( checkMap.get(""+v) != null && v!=999){
			System.err.println( "重复警告：" + cls.getName() +"."+ m.getName() + "  Value:" + v);
		}else{
			checkMap.put(""+v, v) ;
		}
		int pv = auth.pval() ;
		int t = auth.type() ;
//		if( t == 1 ){
//			url = auth.url() ;
//		}
		String str = String.format("INSERT INTO t_menu_v2(menu_id,menu_id_p,menu_name,menu_type,menu_url,"
				+ "menu_class,menu_method,menu_status,menu_desc) VALUES("
				+ "%d,%d,'%s','%s','%s','%s','%s','A','自动生成');", 
				v , pv==-1?v:pv , auth.desc() , t==0?"A":"B" , url ,
						cls.getName() , m.getName());
		if(v!=999){
			System.out.println(str);
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		System.out.println("DELETE FROM t_menu_v2;");
		getControllerNames();
//		Class cls = Class.forName("com.dutiantech.controller.OPUserV2Controller") ;
//		Method[] ms = cls.getMethods() ;
//		for(Method m : ms){
//			Annotation anno = m.getAnnotation( annoClass ) ;
//			if( anno != null ){
//				AuthNum an = (AuthNum)anno ;
//				System.out.println( an.value() );
//			}
//		}
	}

}
