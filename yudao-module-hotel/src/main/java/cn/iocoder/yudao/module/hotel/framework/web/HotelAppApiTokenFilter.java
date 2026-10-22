package cn.iocoder.yudao.module.hotel.framework.web;

import cn.iocoder.yudao.framework.common.biz.system.oauth2.OAuth2TokenCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.config.SecurityProperties;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * 用户 App 接口的 token 容错过滤器。
 *
 * <p>背景：框架的 TokenAuthenticationFilter 会在「admin token 访问 /app-api/**」时
 * 直接抛「错误的用户类型」并返回错误，导致 C 端公开接口（登录、可售查询、下单、支付）
 * 只要携带了管理端 token 就不可用。</p>
 *
 * <p>处理：/app-api/** 请求若携带的不是会员 token，则隐藏 Authorization 头，按匿名请求处理：
 * 公开接口正常放行，需要登录的接口由 Spring Security 返回 401。会员 token 原样保留。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HotelAppApiTokenFilter extends OncePerRequestFilter {

    private static final String APP_API_PREFIX = "/app-api/";

    @Resource
    private SecurityProperties securityProperties;
    @Resource
    private OAuth2TokenCommonApi oauth2TokenApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri == null || !uri.startsWith(APP_API_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }
        String token = SecurityFrameworkUtils.obtainAuthorization(request,
                securityProperties.getTokenHeader(), securityProperties.getTokenParameter());
        if (token == null || token.isEmpty() || isMemberToken(token)) {
            chain.doFilter(request, response);
            return;
        }
        log.debug("[doFilterInternal] 非会员 token 访问 {}，按匿名请求处理", uri);
        chain.doFilter(new AnonymousRequestWrapper(request), response);
    }

    private boolean isMemberToken(String token) {
        try {
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
            return accessToken != null && UserTypeEnum.MEMBER.getValue().equals(accessToken.getUserType());
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * 隐藏 Authorization 头（以及 token 查询参数）的请求包装。
     */
    private static class AnonymousRequestWrapper extends HttpServletRequestWrapper {

        AnonymousRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getHeader(String name) {
            if (isTokenHeader(name)) {
                return null;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (isTokenHeader(name)) {
                return Collections.emptyEnumeration();
            }
            return super.getHeaders(name);
        }

        @Override
        public String getParameter(String name) {
            if ("token".equalsIgnoreCase(name)) {
                return null;
            }
            return super.getParameter(name);
        }

        private boolean isTokenHeader(String name) {
            return name != null && ("authorization".equalsIgnoreCase(name)
                    || "token".equalsIgnoreCase(name));
        }
    }
}
