package com.mmb.dto;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;

import com.mmb.entity.Member;
import com.mmb.service.MemberService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Req {
    private final MemberService memberService;

    private Member loginedMember;
    private Integer loginedMemberId;

    public Integer getLoginedMemberId() {
        if (loginedMemberId != null) {
            return loginedMemberId;
        }
        if (loginedMember == null || loginedMember.getId() == null) {
            return null;
        }
        return loginedMember.getId();
    }

    public boolean isLogined() {
        return getLoginedMemberId() != null;
    }

    /**
     * 관리자 여부 판별:
     * 1) Spring Security 권한에 ROLE_ADMIN / ADMIN 이 있으면 true
     * 2) fallback: username 이 admin 이면 true
     */
    public boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof String s && "anonymousUser".equals(s)) {
            return false;
        }

        // 1) 권한 기반 (정석)
        if (auth.getAuthorities() != null &&
                auth.getAuthorities().stream().anyMatch(a -> {
                    String role = a.getAuthority();
                    return "ROLE_ADMIN".equals(role) || "ADMIN".equals(role);
                })) {
            return true;
        }

        // 2) fallback: admin 계정명
        String username = auth.getName();
        return "admin".equalsIgnoreCase(username);
    }

    public void init() {
        this.loginedMember = null;
        this.loginedMemberId = null;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String s && "anonymousUser".equals(s)) {
            return;
        }

        String username = auth.getName();
        if (!StringUtils.hasText(username)) {
            return;
        }

        Member member = memberService.findByUsername(username).orElse(null);
        if (member == null || member.getId() == null) {
            return;
        }

        this.loginedMember = member;
        this.loginedMemberId = member.getId();
    }

    public String jsPrintReplace(String msg, String uri) {
        return "redirect:" + uri;
    }
}
