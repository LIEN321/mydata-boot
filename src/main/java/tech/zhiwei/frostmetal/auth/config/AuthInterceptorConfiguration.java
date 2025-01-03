package tech.zhiwei.frostmetal.auth.config;

import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tech.zhiwei.frostmetal.auth.interceptor.ApiAuthInterceptor;
import tech.zhiwei.frostmetal.auth.interceptor.TokenInterceptor;

/**
 * 授权拦截配置
 *
 * @author LIEN
 * @since 2024/8/27
 */
@Order
@Configuration(proxyBeanMethods = false)
@AllArgsConstructor
@EnableConfigurationProperties({AuthProperties.class})
public class AuthInterceptorConfiguration implements WebMvcConfigurer {

    private final AuthProperties authProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // token校验的拦截器
        if (authProperties.getEnabled()) {
            registry.addInterceptor(new TokenInterceptor())
                    .excludePathPatterns(authProperties.getDefaultExcludePatterns())
                    .excludePathPatterns(authProperties.getSkipUrl());
        }

        // api权限校验拦截器
        registry.addInterceptor(new ApiAuthInterceptor())
                .excludePathPatterns(authProperties.getDefaultExcludePatterns())
                .excludePathPatterns(authProperties.getSkipUrl());
    }
}
