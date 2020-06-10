package com.micro.cloud.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.micro.cloud.configuration.RedisClient;
import com.micro.common.dto.UserDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * class
 *
 * @author Mr.zxb
 * @date 2020-06-10 14:41
 */
@WebFilter
public class LoginFilter implements Filter {

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

        String token = request.getParameter("token");
        if (StringUtils.isBlank(token)) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equalsIgnoreCase(token)) {
                        token = cookie.getValue();
                    }
                }
            }
        }
        UserDTO userDTO = null;
        if (StringUtils.isNotBlank(token)) {
            // 从缓存里获取
            userDTO = CACHE.getIfPresent(token);
            if (userDTO == null) {
                userDTO = (UserDTO) redisClient.get(token);
                if (userDTO != null) {
                    // 存入缓存
                    CACHE.put(token, userDTO);
                }
            }
        }
        if (userDTO == null) {
            // 跳转登录页面
            response.sendRedirect("http://localhost:8080/user/login");
            return;
        }

        filterChain.doFilter(request, response);
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean(LoginFilter loginFilter) {
        FilterRegistrationBean<Filter> filterFilterRegistrationBean = new FilterRegistrationBean<>();
        filterFilterRegistrationBean.setFilter(loginFilter);

        List<String> urlPatterns = new ArrayList<>();
        urlPatterns.add("/*");
        filterFilterRegistrationBean.setUrlPatterns(urlPatterns);
        return filterFilterRegistrationBean;
    }
}
