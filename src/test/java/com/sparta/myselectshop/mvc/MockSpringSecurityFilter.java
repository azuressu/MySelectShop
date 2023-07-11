package com.sparta.myselectshop.mvc;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class MockSpringSecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // securitycontextHolder가 인증 객체를 담고있는 context를 담고 있는 공간이라 필요함
        // 거기서 getContext() 메소드를 호출하면 sercuritycontext가 반환됨
        // 거기서 setAuthentication을 통해서 인증 객체를 만드는 것이다
        // 이것은 가짜 인증을 하는 행위
        // 이 security의 가짜 filter를 하나 만드는 것임
        // security가 동작을 하면, 방해가되기 때문에 가짜 filter를 만들어서 사용한다
        SecurityContextHolder.getContext()
                .setAuthentication((Authentication) ((HttpServletRequest) req).getUserPrincipal());
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        SecurityContextHolder.clearContext();
    }


}
