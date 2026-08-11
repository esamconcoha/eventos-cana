package com.canabackend.cana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class CanaApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CanaApplication.class);
        // Se registra a mano y no con @Bean para que alcance a escuchar los
        // eventos tempranos, que ocurren antes de que exista el contexto.
        app.addListeners(new StartupProbe());
        app.run(args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4200")
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }

}
