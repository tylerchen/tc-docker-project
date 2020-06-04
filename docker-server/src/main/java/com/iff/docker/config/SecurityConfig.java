/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iff.docker.modules.app.entity.User;
import com.iff.docker.modules.app.service.UserService;
import com.iff.docker.modules.common.Constant;
import com.iff.docker.modules.util.TokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 安全配置
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@Slf4j
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserService userService;

    private static String remoteIp(HttpServletRequest request) {
        if (StringUtils.isNotBlank(request.getHeader("X-Real-IP"))) {
            return request.getHeader("X-Real-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("X-Forwarded-For"))) {
            return request.getHeader("X-Forwarded-For");
        } else if (StringUtils.isNotBlank(request.getHeader("Proxy-Client-IP"))) {
            return request.getHeader("Proxy-Client-IP");
        } else if (StringUtils.isNotBlank(request.getHeader("WL-Proxy-Client-IP"))) {
            return request.getHeader("WL-Proxy-Client-IP");
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 加入自定义的安全认证
        auth.userDetailsService(userDetailsService()).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/", "/asset/**", "/swagger-ui.html", "/webjars/*", "/user/login");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().disable();
        // http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
        // 去掉 CSRF
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用 JWT，关闭session
                //启用基于 HttpServletRequest 的访问限制，开始配置哪些URL需要被保护、哪些不需要被保护
                .and().authorizeRequests().antMatchers("/", "/asset/**", "/swagger-ui.html", "/webjars/*", "/user/login").permitAll()
                .and().httpBasic().authenticationEntryPoint(authenticationEntryPoint())
                .and().authorizeRequests().anyRequest().access("@rbacauthorityservice.hasPermission(request,authentication)") // RBAC 动态 url 认证
                .and().formLogin().loginPage("/asset/api_old/index.html#/user/login").loginProcessingUrl("/login")/*开启登录*/.successHandler(authenticationSuccessHandler()) /*登录成功*/.failureHandler(authenticationFailureHandler()) /*登录失败*/.permitAll()
                .and().logout().logoutSuccessHandler(logoutSuccessHandler()).permitAll();
        // 记住我
        http.rememberMe().rememberMeParameter("remember-me").userDetailsService(userDetailsService()).tokenValiditySeconds(300);
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler()); // 无权访问 JSON 格式的数据
        http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class); // JWT Filter
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                //构建用户信息的逻辑(取数据库/LDAP等用户信息)
                User user = userService.findByUserName(username);
                if (user == null) {
                    return null;
                }
                org.springframework.security.core.userdetails.User userDetails = null;
                {
                    Set<GrantedAuthority> authoritiesSet = new HashSet();
                    if ("root".equals(user.getUserName())) {
                        authoritiesSet.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    } else {
                        authoritiesSet.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                    //构建用户信息的逻辑(取数据库/LDAP等用户信息)
                    userDetails = new org.springframework.security.core.userdetails.User(user.getUserName(), new BCryptPasswordEncoder().encode(user.getPassword()), authoritiesSet);
                }
                return userDetails;
            }
        };
    }

    public AccessDeniedHandler accessDeniedHandler() {
        return new AccessDeniedHandler() {
            public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                JSONObject map = new JSONObject();
                {
                    map.put("status", 300);
                    map.put("message", "Need Authorities");
                }
                httpServletResponse.getWriter().write(JSON.toJSONString(map));
            }
        };
    }

    public LogoutSuccessHandler logoutSuccessHandler() {
        return new LogoutSuccessHandler() {
            public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
                JSONObject map = new JSONObject();
                {
                    map.put("status", 100);
                    map.put("message", "Logout Success");
                }
                httpServletResponse.getWriter().write(JSON.toJSONString(map));
            }
        };
    }

    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureHandler() {
            public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                JSONObject map = new JSONObject();
                {
                    map.put("status", 400);
                    map.put("message", "Login Failure");
                }
                httpServletResponse.getWriter().write(JSON.toJSONString(map));
            }
        };
    }

    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                JSONObject map = new JSONObject();
                {
                    map.put("status", 200);
                    map.put("message", "Login Success");
                }
                {
                    org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
                    User user = getUser(userDetails.getUsername(), request);
                    String token = TokenUtil.toToken(userDetails.getUsername(), user.getPassword());
                    map.put("token", token);
                    response.setHeader("token", token);
                    //response.addHeader("Set-Cookie", "_token="+token);
                    Cookie cookie = new Cookie("_token", token);
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    response.addCookie(cookie);
                }
                response.getWriter().write(JSON.toJSONString(map));
            }
        };
    }

    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                JSONObject map = new JSONObject();
                {
                    map.put("status", 401);
                    map.put("message", "Need Authorities");
                }
                httpServletResponse.getWriter().write(JSON.toJSONString(map));
            }
        };
    }

    private void send401(HttpServletResponse response) throws IOException {
        JSONObject map = new JSONObject();
        {
            map.put("status", 401);
            map.put("message", "Need Authorities");
        }
        response.getWriter().write(JSON.toJSONString(map));
    }

    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
                if ("POST".equals(request.getMethod().toUpperCase()) && "/login".equals(request.getServletPath())) {
                    chain.doFilter(request, response);
                    return;
                }
                String token = request.getHeader("X-Access-Token");
                {//添加默认登录的帐号
                    token = TokenUtil.toToken("admin", "admin");
                }
                if (StringUtils.isBlank(token) || StringUtils.equals(token, "null")) {
                    token = request.getParameter("_token");
                }
                if ((StringUtils.isBlank(token) || StringUtils.equals(token, "null")) && request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        if ("_token".equals(cookie.getName())) {
                            token = cookie.getValue();
                        }
                    }
                }
                if (token == null || StringUtils.equals(token, "null") /*|| !authHeader.startsWith("Bearer ")*/) {
                    send401(response);
                    return;
                }

                final String authToken = token;//authHeader.substring("Bearer ".length());
                String username = TokenUtil.getUserId(authToken);
                if (StringUtils.isBlank(username) || SecurityContextHolder.getContext().getAuthentication() != null) {
                    send401(response);
                    return;
                }
                UserDetails userDetails = userDetailsService().loadUserByUsername(username);
                if (userDetails == null) {
                    send401(response);
                    return;
                }
                User user = getUser(userDetails.getUsername(), request);
                if (!TokenUtil.verifyToken(authToken, user.getPassword())) {
                    send401(response);
                    return;
                }

                log.debug(user.getUserName() + " " + request.getMethod() + " " + request.getRequestURI() + "?" + paramsToString(request));

                {
                    String renewToken = TokenUtil.renewToken(authToken, user.getUserName(), user.getPassword());
                    if (renewToken != null) {
                        response.setHeader("token", renewToken);
                        Cookie cookie = new Cookie("_token", renewToken);
                        cookie.setPath("/");
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie);
                    }
                    boolean hasTokenCookie = false;
                    if (request.getCookies() != null) {
                        for (Cookie cookie : request.getCookies()) {
                            if ("_token".equals(cookie.getName())) {
                                hasTokenCookie = true;
                                break;
                            }
                        }
                    }
                    if (renewToken == null && !hasTokenCookie) {
                        response.setHeader("token", token);
                        Cookie cookie = new Cookie("_token", token);
                        cookie.setPath("/");
                        cookie.setHttpOnly(true);
                        response.addCookie(cookie);
                    }
                }
                {
                    request.setAttribute(Constant.LOGIN_TOKEN, token);
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                chain.doFilter(request, response);
            }
        };
    }

    private User getUser(String username, HttpServletRequest request) {
        User user = (User) request.getAttribute(Constant.LOGIN_USER);
        if (user == null) {
            user = userService.findByUserName(username);
            request.setAttribute(Constant.LOGIN_USER, user);
        }
        return user;
    }

    private String paramsToString(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            sb.append(entry.getKey()).append("=");
            String[] value = entry.getValue();
            if (value == null || value.length < 1) {
                sb.append("\n");
                continue;
            }
            if (value.length == 1) {
                sb.append(value[0]).append("\n");
                continue;
            }
            sb.append(StringUtils.join(value, ",")).append("\n");
        }
        if (sb.length() > 2000) {
            sb.setLength(2000);
            int lastIndexOf = sb.lastIndexOf("\n");
            if (lastIndexOf > 0) {
                sb.setLength(lastIndexOf);
            }
        }
        return sb.toString();
    }

    @Component("rbacauthorityservice")
    public class RbacAuthorityService {
        public boolean hasPermission(HttpServletRequest request, Authentication authentication) {
            Object userInfo = authentication.getPrincipal();
            boolean hasPermission = false;
            if (!(userInfo instanceof UserDetails)) {
                return false;
            }
            String username = ((UserDetails) userInfo).getUsername();

            //获取资源
            Set<String> urls = new HashSet();
            urls.add("/**"); // 这些 url 都是要登录后才能访问，且其他的 url 都不能访问！

            AntPathMatcher antPathMatcher = new AntPathMatcher();

            for (String url : urls) {
                if (antPathMatcher.match(url, request.getRequestURI())) {
                    hasPermission = true;
                    break;
                }
            }
            return hasPermission;
        }
    }
    /**
     * https://www.baeldung.com/spring-security-method-security
     */
//    3、@EnableGlobalMethodSecurity详解
//
//3.1、@EnableGlobalMethodSecurity(securedEnabled=true) 开启@Secured 注解过滤权限
//
//3.2、@EnableGlobalMethodSecurity(jsr250Enabled=true)开启@RolesAllowed 注解过滤权限 
//
//3.3、@EnableGlobalMethodSecurity(prePostEnabled=true) 使用表达式时间方法级别的安全性         4个注解可用
//
//    @PreAuthorize 在方法调用之前,基于表达式的计算结果来限制对方法的访问
//
//    @PostAuthorize 允许方法调用,但是如果表达式计算结果为false,将抛出一个安全性异常
//
//    @PostFilter 允许方法调用,但必须按照表达式来过滤方法的结果
//
//    @PreFilter 允许方法调用,但必须在进入方法之前过滤输入值
//
//    作者：谁在烽烟彼岸
//    链接：https://www.jianshu.com/p/41b7c3fb00e0
//    来源：简书
//    著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
}
