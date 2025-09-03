package com.uijthof.setu_diff.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Deze configuratieklasse lost het "Failed to fetch" probleem op.
 * Het stelt de Spring Boot backend in staat om verzoeken van de
 * frontend (uw HTML-bestand) te accepteren.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Pas toe op alle endpoints in de applicatie (zoals /search-by-id)
                .allowedOrigins("*") // Sta verzoeken van ELKE oorsprong toe (inclusief file://)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Sta de benodigde HTTP-methodes toe
                .allowedHeaders("*"); // Sta alle HTTP-headers toe
    }
}
