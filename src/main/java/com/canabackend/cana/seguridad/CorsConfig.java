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

        // Sin esto, el navegador le esconde Content-Disposition al JavaScript:
        // solo deja leer las 7 cabeceras "safelisted" (Content-Type, Expires...)
        // y cualquier otra hay que declararla aca. El front la usa para sacar el
        // nombre del archivo al descargar cotizaciones, pedidos, constancias y el
        // PDF de reportes; mientras front y back estaban en el mismo origen no se
        // notaba, pero con el front en Vercel devolveria null en todos esos casos.
        config.setExposedHeaders(List.of("Content-Disposition"));
        // El preflight (OPTIONS) de cada endpoint se cachea una hora en el
        // navegador. Sin esto, cada POST/PUT/DELETE paga dos viajes a Fly.
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
