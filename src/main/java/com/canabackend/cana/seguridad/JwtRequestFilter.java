package com.canabackend.cana.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtilService jwtUtil;
    private static Logger logger
            = Logger.getLogger(
            JwtRequestFilter.class.getName());
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtilService jwtUtilService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
           logger.log(Level.INFO, "Se ejecuta el metodo doFilterInternal");
            String authorizationHeader = request.getHeader("Authorization");
            logger.log(Level.INFO, "Authorization header recibido: " + authorizationHeader);
            if(authorizationHeader != null && authorizationHeader.startsWith("Bearer")){
                String token = authorizationHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if(jwtUtil.validateToken(token, userDetails)){
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }

            filterChain.doFilter(request, response);
        }catch (ServletException e) {
            logger.log(Level.WARNING, "Error en el filtro de autorización", e);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"NO_AUTORIZADO\", \"message\": \""+ "\"}");
        }
    }
}
