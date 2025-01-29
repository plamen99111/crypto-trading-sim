package com.example.cryptosim.config;

import com.example.cryptosim.exception.InvalidJwtTokenException;
import com.example.cryptosim.exception.UnauthorizedException;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;

    JwtAuthFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException, InvalidJwtTokenException {
        String token = getJwtFromRequest(request);

        if (token != null && tokenProvider.validateToken(token)) {

            String username = tokenProvider.getUsernameFromJWT(token);
            String role = tokenProvider.getRoleFromJWT(token);

            if (username != null && role != null) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
                Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            if (username == null || role == null) {
                LOGGER.error("Username or role are missing");
                throw new UnauthorizedException("Missing required JWT claims.");
            }
            if (!tokenProvider.validateToken(token)) {
                LOGGER.error("Invalid JWT token.");
                throw new UnauthorizedException("Invalid JWT token.");
            }
            if (tokenProvider.isTokenExpired(token)) {
                LOGGER.error("Expired JWT token.");
                throw new UnauthorizedException("JWT token has expired.");
            }
        }
        chain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // Try to get the token from the Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring("Bearer ".length());
        }

        // Fallback to reading the token from cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}

