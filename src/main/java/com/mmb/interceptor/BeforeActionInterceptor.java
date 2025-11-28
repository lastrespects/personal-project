// BeforeActionInterceptor.java
package com.mmb.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mmb.dto.Req; // 패키지 변경

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class BeforeActionInterceptor implements HandlerInterceptor {
	
	private Req req; // com.mmb.dto.Req
	
	public BeforeActionInterceptor(Req req) {
		this.req = req;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		
		this.req.init();
		
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
	
}