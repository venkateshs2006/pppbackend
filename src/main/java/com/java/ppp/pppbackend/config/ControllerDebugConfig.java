package com.java.ppp.pppbackend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@Configuration
@Slf4j
public class ControllerDebugConfig {

    @Bean
    public CommandLineRunner listAllEndpoints(ApplicationContext applicationContext) {
        return args -> {
            RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
            Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();

            log.info("=== ALL REGISTERED ENDPOINTS ===");
            map.forEach((info, method) -> {
                log.info("{} -> {}.{}",
                        info.getPatternsCondition(),
                        method.getBeanType().getSimpleName(),
                        method.getMethod().getName());
            });
            log.info("=== TOTAL ENDPOINTS: {} ===", map.size());
        };
    }
}