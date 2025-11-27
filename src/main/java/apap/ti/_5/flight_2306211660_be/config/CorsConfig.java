package apap.ti._5.flight_2306211660_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Value("${CORS_ALLOWED_ORIGINS:http://2306211660-be.hafizmuh.site,http://2306211660-fe.hafizmuh.site,http://2306212083-be.hafizmuh.site,http://2306212083-fe.hafizmuh.site,http://2306203236-be.hafizmuh.site,http://2306203236-fe.hafizmuh.site,http://2306240061-be.hafizmuh.site,http://2306240061-fe.hafizmuh.site,http://2306219575-be.hafizmuh.site,http://2306219575-fe.hafizmuh.site,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = allowedOrigins.split(",");
                

                for (int i = 0; i < origins.length; i++) {
                    origins[i] = origins[i].trim();
                }

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .exposedHeaders("Authorization");
            }
        };
    }
}
