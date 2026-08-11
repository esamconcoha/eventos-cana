package com.canabackend.cana.seguridad;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Unica configuracion de CORS de la aplicacion. Antes habia dos y ninguna
 * servia: esta clase declaraba corsConfigurer() sin @Bean (o sea que Spring
 * nunca la llamaba) y CanaApplication tenia otra copia con el origen
 * "http://localhost:4200" quemado en el codigo.
 *
 * Se expone como CorsConfigurationSource y no como WebMvcConfigurer porque es
 * lo que consume el .cors(withDefaults()) de WebSecurityConfig: asi el filtro
 * de Spring Security responde los preflight con estos origenes.
 */
@Configuration
public class CorsConfig {

    private final List<String> origenesPermitidos;

    public CorsConfig(@Value("${cors.allowed-origins}") List<String> origenesPermitidos) {
        this.origenesPermitidos = origenesPermitidos;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // setAllowedOriginPatterns en vez de setAllowedOrigins: acepta igual los
        // origenes exactos, pero ademas permite comodines si mas adelante hacen
        // falta (ej. "https://*.vercel.app" para los preview de Vercel).
        config.setAllowedOriginPatterns(origenesPermitidos);
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        // No se activa allowCredentials: el front manda el JWT en la cabecera
        // Authorization, no en una cookie. Activarlo ademas prohibiria los
        // comodines de arriba.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
