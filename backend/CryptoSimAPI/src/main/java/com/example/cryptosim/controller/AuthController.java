package com.example.cryptosim.controller;

import com.example.cryptosim.exception.InvalidCredentialsException;
import com.example.cryptosim.exception.InvalidJwtTokenException;
import com.example.cryptosim.model.User;
import com.example.cryptosim.payload.JwtResponse;
import com.example.cryptosim.payload.UserRequest;
import com.example.cryptosim.service.AuthService;
import com.example.cryptosim.config.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Value("${isProduction}")
    private boolean isProduction;

    @Value("${isCookieStrict}")
    private boolean isCookieStrict;

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody UserRequest userRequest)
        throws InvalidCredentialsException {
        LOGGER.info("Authentication attempt for username: {}", userRequest.getUsername());

        User user = authService.authenticateUser(userRequest.getUsername(), userRequest.getPassword());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().getRoleName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername(), user.getRole().getRoleName());

        LOGGER.info("Authentication successful for username: {}", userRequest.getUsername());

        // Set refresh token as an HttpOnly cookie
        HttpServletResponse response =
            ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(isProduction);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge((int) jwtTokenProvider.getRefreshTokenExpiration());
        response.addCookie(refreshCookie);


        response.addHeader("Set-Cookie",
            String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; Secure; SameSite=%s", refreshToken,
                (int) jwtTokenProvider.getRefreshTokenExpiration(), isCookieStrict ? "Strict" : "None"));


        // Send access token in the response body
        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) throws InvalidJwtTokenException {
        SecurityContextHolder.clearContext();
        // Invalidate the refreshToken cookie
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(isProduction);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // Expire immediately
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("Logged out successfully.");
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) throws InvalidJwtTokenException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    if (jwtTokenProvider.validateToken(refreshToken)) {
                        String username = jwtTokenProvider.getUsernameFromJWT(refreshToken);
                        System.out.println(username);
                        String newAccessToken = jwtTokenProvider.generateAccessToken(username,
                            jwtTokenProvider.getRoleFromJWTWithoutPrefixRole(refreshToken));
                        System.out.println(jwtTokenProvider.getRoleFromJWT(refreshToken));
                        return ResponseEntity.ok(new JwtResponse(newAccessToken));
                    }
                }
            }
        }
        throw new InvalidJwtTokenException("Invalid or missing refresh token.");
    }

}