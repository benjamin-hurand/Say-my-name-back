// src/main/java/.../config/StaticPhotoConfig.java
package com.saymyname.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.nio.file.Paths;

@Configuration
public class StaticPhotoConfig implements WebMvcConfigurer {
    @Value("${photos.storage.root}")
    private String rootDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uri = Paths.get(rootDir).toUri().toString(); // "file:/.../photos/"
        registry.addResourceHandler("/photos/**")
                .addResourceLocations(uri)
                .setCachePeriod(31556926); // 1 an
    }
}
