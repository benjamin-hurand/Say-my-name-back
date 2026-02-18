package com.saymyname.webapp.config;

import com.saymyname.webapp.multitenancy.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final TenantInterceptor tenantInterceptor;

    public WebMvcConfig(TenantInterceptor ti) {
        this.tenantInterceptor = ti;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**", // login, google login, refresh, reset...
                        "/actuator/**",
                        "/error", // handler d'erreurs
                        "/swagger-ui/**", "/v3/api-docs/**" // si tu les exposes
                );
    }
}
