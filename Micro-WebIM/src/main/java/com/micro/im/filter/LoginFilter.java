package com.micro.im.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.micro.im.configuration.RedisClient;
import com.micro.common.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-10 14:41
 */
@Slf4j
@WebFilter
public class LoginFilter implements Filter {

    private static final Set<String> ALLOWED_PATHS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("/login", "/logout", "/register", "/accountExists", "/js/", "/css/", "/image/", "/layui", "*.ico")));

    /**
     * 缓存实例
     */
    private static final Cache<String, UserDTO> CACHE = CacheBuilder.newBuilder().maximumSize(10000)
            .expireAfterWrite(3, TimeUnit.MINUTES).build();

    @Autowired
    private RedisClient redisClient;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();
        if (pass(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
//        String token = request.getHeader("token");
        String token = (String) ((HttpServletRequest) servletRequest).getSession().getAttribute("token");
        if (StringUtils.isBlank(token)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase("token")) {
                        token = cookie.getValue();
                    }
                }
            }
            if (StringUtils.isBlank(token)) {
                // 跳转登录页面
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }
        // 从缓存里获取
        UserDTO userDTO = CACHE.getIfPresent(token);
        if (userDTO == null) {
            if (StringUtils.isNotBlank(token)) {
                userDTO = (UserDTO) redisClient.get(token);
                if (userDTO != null) {
                    // 存入缓存
                    CACHE.put(token, userDTO);
                } else {
                    // 跳转登录页面
                    response.sendRedirect(request.getContextPath() + "/login");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    public boolean pass(String uri) {
        for (String path : ALLOWED_PATHS) {
            if (uri.contains(path)) {
                return true;
            }
        }
        return false;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(new LoginFilter());
        filterFilterRegistrationBean.addUrlPatterns("/*");
        return filterFilterRegistrationBean;
    }
}
