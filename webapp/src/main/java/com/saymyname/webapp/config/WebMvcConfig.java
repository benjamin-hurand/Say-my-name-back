package com.saymyname.webapp.config;

import com.saymyname.webapp.multitenancy.OrgInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final OrgInterceptor orgInterceptor;

    public WebMvcConfig(OrgInterceptor oi) {
        this.orgInterceptor = oi;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orgInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**", // login, google login, refresh, reset...
                        "/actuator/**",
                        "/error", // handler d'erreurs
                        "/swagger-ui/**", "/v3/api-docs/**" // si tu les exposes
                );
    }
}
