/*******************************************************************************
 * Copyright (c) 2019-10-29 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/
package com.iff.docker.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Web 端配置，默认不生效（默认为微服务）
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-29
 * auto generate by qdp.
 */
@ConditionalOnProperty(name = "config.web.enable", havingValue = "true")
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        //主页
        //registry.addViewController("/").setViewName("forward:/index");
        registry.addRedirectViewController("/", "/asset/api/index.html");
    }

    /**
     * 国际化
     *
     * @return
     */
    @Bean(name = "messageSource")
    public ResourceBundleMessageSource getMessageSource() {
        ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();
        resourceBundleMessageSource.setDefaultEncoding("UTF-8");
        resourceBundleMessageSource.setBasenames("i18n/messages", "i18n/ValidationMessages");
        resourceBundleMessageSource.setCacheSeconds(3600);
        return resourceBundleMessageSource;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                //.allowedMethods("*")
                //.allowedHeaders("*")
                .allowedMethods("GET", "POST", "DELETE", "PUT", "PATCH")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解决 swagger-ui.html 404报错
        registry.addResourceHandler("/asset/**", "/swagger-ui.html")
                .addResourceLocations("classpath:/asset/", "classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 跨域拦截器需放在最上面
        registry.addInterceptor(new CorsInterceptor()).addPathPatterns("/**");
        //registry.addInterceptor(new WebLogInterceptor()).addPathPatterns("/**").excludePathPatterns("/user/login");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        {
            MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
            List<MediaType> list = new ArrayList<>();
            list.add(MediaType.APPLICATION_JSON_UTF8);
            list.add(new MediaType("text", "html", Charset.forName("UTF-8")));
            list.add(new MediaType("application", "*+json", Charset.forName("UTF-8")));
            converter.setSupportedMediaTypes(list);
            converters.add(converter);
        }
    }

    public class CorsInterceptor implements HandlerInterceptor {
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS");
            response.setHeader("Access-Control-Max-Age", "86400");
            response.setHeader("Access-Control-Allow-Headers", "*");
            // 如果是OPTIONS则结束请求
            if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
                response.setStatus(HttpStatus.NO_CONTENT.value());
                return false;
            }
            return true;
        }
    }
}
