// NeedLoginInterceptor.java
package com.mmb.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.mmb.dto.Req; // 패키지 변경

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class NeedLoginInterceptor implements HandlerInterceptor {

	private Req req;

	public NeedLoginInterceptor(Req req) {
		this.req = req;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// Spring Security 로그인 여부 우선 체크
		if (request.getUserPrincipal() != null) {
			return HandlerInterceptor.super.preHandle(request, response, handler);
		}

		// 세션에 별도로 심어 둔 loginedMember 기준 체크
		if (this.req.getLoginedMember() != null && this.req.getLoginedMember().getId() != 0) {
			return HandlerInterceptor.super.preHandle(request, response, handler);
		}

		this.req.jsPrintReplace("로그인 후 이용해주세요", "/login");
		return false;
	}

}
