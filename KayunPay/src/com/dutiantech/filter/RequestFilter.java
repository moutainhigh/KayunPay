package com.dutiantech.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestFilter implements Filter {

	private String ps = ".html|.jsp";
	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest)arg0 ;
		String url = request.getRequestURI() ;
		
		String[] pss = ps.split("\\|") ;
		boolean isOk = true ;
		for(String pKey : pss ){
			if( url.endsWith(pKey) == true ){
				isOk = false ;
				break ;
			}
		}
		
		if( isOk ){
			arg2.doFilter(arg0, arg1); 
		}else{
			HttpServletResponse response = (HttpServletResponse)arg1 ;
			PrintWriter pw = response.getWriter() ;
			pw.write("<div style='display:none'>HelloKitty</div>");
			pw.flush();
			pw.close();
			response.flushBuffer(); 
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		String val = arg0.getInitParameter("ps");
		if(val != null ){
			ps = val ;
		}
	}

}
