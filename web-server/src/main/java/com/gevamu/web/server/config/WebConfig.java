package com.gevamu.web.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebFluxConfigurer {

    private static final String STATIC_CONTENT_DIRECTORY_NAME = "static";
    private static final String REST_API_PREFIX = "/api/v1";

    @Value("${static_resources_path:}")
    private transient String staticResourcesPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path staticPath = StringUtils.isNotBlank(staticResourcesPath) ?
            Paths.get(staticResourcesPath) :
            Paths.get(System.getProperty("user.dir"), STATIC_CONTENT_DIRECTORY_NAME);
        String staticPathLocation = "file:" + staticPath + "/";
        registry.addResourceHandler("/**")
            .addResourceLocations(staticPathLocation)
            .resourceChain(true);
        log.info("Static content location: {}", staticPathLocation);
    }

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(REST_API_PREFIX, HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
