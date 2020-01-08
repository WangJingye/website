package com.delcache.interceptor;

import com.delcache.component.sitemesh.WebSiteMeshFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private AuthFilter authFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 自定义拦截器，添加拦截路径和排除拦截路径
        registry.addInterceptor(authFilter)
                .addPathPatterns("/**")
                .excludePathPatterns("/login.html")
                .excludePathPatterns("/static/**")
                .excludePathPatterns("/layouts/**")
                .excludePathPatterns("/upload/**")
                .excludePathPatterns("/favicon.ico")
                .excludePathPatterns("/error.html")
                .excludePathPatterns("/logout.html");
    }
    @Bean
    public FilterRegistrationBean siteMeshFilter() {
        FilterRegistrationBean fitler = new FilterRegistrationBean();
        WebSiteMeshFilter siteMeshFilter = new WebSiteMeshFilter();
        fitler.setFilter(siteMeshFilter);
        return fitler;
    }
    @Override
    public void addViewControllers(ViewControllerRegistry registry){
        //设置系统访问的默认首页
        registry.addViewController("/").setViewName("redirect:/erp/site-info/edit.html");
    }
    //静态资源目录
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //需要配置1：----------- 需要告知系统，这是要被当成静态文件的！
        //第一个方法设置访问路径前缀，第二个方法设置资源路径
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
        registry.addResourceHandler("/upload/**").addResourceLocations("/upload/");
    }
}
