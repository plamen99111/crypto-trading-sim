package com.example.cryptosim.config;

import com.example.cryptosim.exception.GenerateTokenException;
import com.example.cryptosim.exception.InvalidJwtTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    @Value("${jwtAccessTokenExpiration}")
    private long jwtAccessTokenExpirationInMs;

    @Value("${jwtRefreshTokenExpiration}")
    private long jwtRefreshTokenExpirationInMs;

    @Value("${jwt.privateKeyPath}")
    private String privateKeyPath;

    @Value("${jwt.publicKeyPath}")
    private String publicKeyPath;

    // Constructor to load RSA keys from files
    @PostConstruct
    public void init() throws Exception {
        this.privateKey = (RSAPrivateKey) loadKey(privateKeyPath, true);
        this.publicKey = (RSAPublicKey) loadKey(publicKeyPath, false);
    }

    private Key loadKey(String filePath, boolean isPrivateKey) throws IOException {
        try {
            byte[] keyBytes = Files.readAllBytes(Paths.get(filePath));
            String keyContent =
                new String(keyBytes).replace("-----BEGIN " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
                    .replace("-----END " + (isPrivateKey ? "PRIVATE" : "PUBLIC") + " KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decodedKey = Base64.getDecoder().decode(keyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            if (isPrivateKey) {
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
            } else {
                return keyFactory.generatePublic(new X509EncodedKeySpec(decodedKey));
            }
        } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            LOGGER.error("Failed to load key from path: {}", filePath, e);
            throw new RuntimeException("Failed to load key", e);
        }
    }

    public String generateAccessToken(String username, String role) throws GenerateTokenException {
        long expirationTime = System.currentTimeMillis() + jwtAccessTokenExpirationInMs;

        try {
            return Jwts.builder().subject(username).claim("role", "ROLE_" + role) // Store role information as a claim
                .issuedAt(new Date()).expiration(new Date(expirationTime))
                .signWith(privateKey) // Sign with RSA private key
                .compact();
        } catch (Exception e) {
            LOGGER.error("JWT access token generation failed for user: {} with role: {}", username, role, e);
            throw new GenerateTokenException("JWT access token generation failed", e);
        }
    }

    public String generateRefreshToken(String username, String role) throws GenerateTokenException {
        long expirationTime = System.currentTimeMillis() + jwtRefreshTokenExpirationInMs;

        try {
            return Jwts.builder().subject(username).claim("role", "ROLE_" + role).issuedAt(new Date())
                .expiration(new Date(expirationTime)).signWith(privateKey) // Sign with RSA private key
                .compact();
        } catch (Exception e) {
            LOGGER.error("JWT refresh token generation failed for user: {} with role: {}", username, role, e);
            throw new GenerateTokenException("JWT refresh token generation failed", e);
        }
    }

    public long getRefreshTokenExpiration() {
        return jwtRefreshTokenExpirationInMs / 1000;
    }

    public String getUsernameFromJWT(String token) {
        return extractClaims(token).getSubject();
    }

    public String getRoleFromJWT(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String getRoleFromJWTWithoutPrefixRole(String token) {
        String role = extractClaims(token).get("role", String.class);
        if (role != null && role.startsWith("ROLE_")) {
            return role.substring(5);  // Removes the "ROLE_" prefix (which has 5 characters)
        }
        return role;  // If there's no prefix, return the role as is
    }

    private Claims extractClaims(String token) {
        try {
            return parseToken(token);
        } catch (InvalidJwtTokenException e) {
            LOGGER.error("Failed to extract claims from JWT token. Token: {}", token, e);
            throw e;
        }
    }

    private Claims parseToken(String token) throws JwtException {
        try {
            return Jwts.parser().verifyWith(publicKey) // Use public key for verification
                .build().parseSignedClaims(token).getPayload();
        } catch (JwtException e) {
            LOGGER.error("JWT parsing/validation failed. Token: {}", token, e);
            throw new InvalidJwtTokenException("Invalid JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token); // Parsing validates the signature and expiration
            return true;
        } catch (InvalidJwtTokenException e) {
            LOGGER.warn("Invalid JWT token detected. Token: {}", token, e);
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (InvalidJwtTokenException e) {
            LOGGER.error("Failed to check if JWT token is expired. Token {}", token, e);
            return false;
        }
    }
}