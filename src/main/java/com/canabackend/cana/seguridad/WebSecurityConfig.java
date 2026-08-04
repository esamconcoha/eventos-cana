package com.canabackend.cana.seguridad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    @Bean
    SecurityFilterChain web(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF para APIs stateless
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/publico/**", "/ws/**","/chat-socket/**").permitAll()  // Permitir rutas públicas y WebSocket
                        .anyRequest().authenticated()  // Proteger el resto
                )
                .cors(withDefaults())  // Permitir CORS
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)  // Filtro JWT
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Sin estado
                );
        return http.build();
    }
  /* (1) By default, Spring Security form login/http basic auth are enabled.
  However, as soon as any servlet-based configuration is provided,
  form based login or/and http basic auth must be explicitly provided.

  * (2) If our stateless API uses token-based authentication, such as JWT,
    we don't need CSRF protection
  */

    // Autenticacion con UserDetailsService
  /*
  @Bean
  UserDetailsServiceImpl userDetailsService() {
    return new UserDetailsServiceImpl();
  }*/

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

  /* Autenticacion en Memoria
  @Bean
  public UserDetailsService users() {
    UserDetails user = User.builder()
        .username("user")
        .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
        .roles("USER")
        .build();
    UserDetails admin = User.builder()
        .username("admin")
        .password("{bcrypt}$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW")
        .roles("USER", "ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user, admin);
  }
  */

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration
                                                        authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
