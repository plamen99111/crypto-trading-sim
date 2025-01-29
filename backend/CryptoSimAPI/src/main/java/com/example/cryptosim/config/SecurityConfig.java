package com.example.cryptosim.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.SecureRandom;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Value("${security.authenticationEntryPointName}")
    private String authenticationEntryPointName;

    @Value("${isProduction}")
    private boolean isProduction;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        String[] publicUrls =
            {"/api/health", "/cryptocurrencies/**", "/cryptocurrencies", "/api/cryptocurrencies/**", "/api/auth/**",
                "/api/logs",};

        http.cors(
                httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable).authorizeHttpRequests(rq -> {
                for (String url : publicUrls) {
                    rq.requestMatchers(url).permitAll();
                }
                rq.requestMatchers("/api/user/**").hasRole("USER");
                rq.requestMatchers("/api/user/crypto/**").hasRole("USER");
                rq.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                rq.anyRequest().authenticated();
            });
        http.sessionManagement(sessionAuthenticationStrategy -> sessionAuthenticationStrategy.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        if (isProduction) {
            http.requiresChannel(channel -> channel.anyRequest().requiresSecure());
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
        throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        final int passwordEncoderStrength = 12;
        SecureRandom secureRandom = new SecureRandom();
        return new BCryptPasswordEncoder(passwordEncoderStrength, secureRandom);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        List<String> allowedOriginsList = List.of(allowedOrigins.split(","));
        corsConfiguration.setAllowedOrigins(allowedOriginsList);
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    // Custom Authentication EntryPoint for Unauthorized Access (401)
    @Bean
    public BasicAuthenticationEntryPoint authenticationEntryPoint() {
        BasicAuthenticationEntryPoint entryPoint = new BasicAuthenticationEntryPoint();
        entryPoint.setRealmName("CryptoSim API");
        return entryPoint;
    }

    // Custom AccessDeniedHandler for Forbidden Access (403)
    @Bean
    public org.springframework.security.web.access.AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
        };
    }

    // Logging unauthorized access attempts
    private void logUnauthorizedAccess(HttpServletRequest request) {
        Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
        logger.warn("Unauthorized access attempt to: {}, from {} ", request.getRequestURI(), request.getRemoteAddr());
    }
}
