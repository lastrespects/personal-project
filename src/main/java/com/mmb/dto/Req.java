package com.mmb.dto;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.mmb.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Req {
    private Member loginedMember;

    public Integer getLoginedMemberId() {
        if (loginedMember == null || loginedMember.getId() == null) {
            return null;
        }
        return loginedMember.getId().intValue();
    }

    public void init() {
        // placeholder for per-request initialization logic
    }

    public String jsPrintReplace(String msg, String uri) {
        // helper for returning a redirect response script
        return "redirect:" + uri;
    }
}
