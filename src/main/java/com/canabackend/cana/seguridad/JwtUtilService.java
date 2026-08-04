package com.canabackend.cana.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtUtilService {

    private static final String SECRET_KEY = "U3RhX2VzX3VuYV9jbGF2ZV9tdXlfc2VndXJhX3BhcmFfQ2FuYV9GbG9yaXN0ZXJpYV95X2N1bXBsZV9jb25fMjU2X2JpdHM=";
    public static final long JWT_TOKEN_VALIDITY = 1000 * 60 * 60 * 8; // 8 Horas

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        return userDetails.getUsername().equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public String extractUsername(String token){
        return getClaims(token).getSubject();
    }

    public boolean isTokenExpired(String token){
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token){
        // Convierte tu String SECRET_KEY a un objeto Key válido
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));

        return Jwts.parser()
                .verifyWith(key)           // Usa el objeto Key
                .build()                   // Construye el parser
                .parseSignedClaims(token)  // Método nuevo
                .getPayload();
    }
}
