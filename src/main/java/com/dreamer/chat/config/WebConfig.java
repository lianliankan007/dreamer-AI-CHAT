package com.dreamer.chat.config;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Web配置类
 * 处理静态资源映射和路由配置
 * 
 * @author panshenguo
 * @since 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置静态资源处理器
     * 处理前端静态文件的访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置静态资源映射
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);

                        // 如果请求的资源存在，直接返回
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // 对于SPA应用，如果资源不存在且不是API请求，返回index.html
                        if (!resourcePath.startsWith("api/") &&
                                !resourcePath.contains(".") &&
                                !resourcePath.equals("favicon.ico")) {
                            return new ClassPathResource("/static/index.html");
                        }

                        return null;
                    }
                });
    }

    /**
     * 配置视图控制器
     * 处理根路径重定向
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 根路径重定向到index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}