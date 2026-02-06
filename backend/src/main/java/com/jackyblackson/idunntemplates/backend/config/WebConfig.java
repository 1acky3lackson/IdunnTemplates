package com.jackyblackson.idunntemplates.backend.config;

import com.jackyblackson.idunntemplates.backend.interceptor.AuthInterceptor;
import com.jackyblackson.idunntemplates.backend.resolver.UserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final UserArgumentResolver userArgumentResolver;

    public WebConfig(AuthInterceptor authInterceptor, UserArgumentResolver userArgumentResolver) {
        this.authInterceptor = authInterceptor;
        this.userArgumentResolver = userArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有路径生效
        registry.addMapping("/**")
                // 使用 allowedOriginPatterns 而不是 allowedOrigins
                // 这样可以利用通配符 * 匹配 localhost 下的任意端口
                .allowedOriginPatterns(
                        "*",
                        "**",
                        "http://localhost:5173/",
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                // 允许的 HTTP 方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许携带凭证（Cookie/Session/Auth Header）
                // 注意：如果设为 true，则 allowedOrigins 不能为 "*" (必须指定具体域名或使用 Pattern)
                .allowCredentials(true)
                // 预检请求(OPTIONS)的缓存时间，单位秒
                .maxAge(3600);
    }
}
