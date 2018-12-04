package com.dutiantech.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthNum {
	int value() default 255;
	int pval() default -1 ;
	int type() default 0 ; // 0 - 菜单   1 - 请求
	String url() default "" ; //当type=1时，url有意义 type=0时，取actionKey的值
	String desc() default "" ;	//当type=0时,desc为name
}
