package com.example.cryptosim.service;

import com.example.cryptosim.exception.InvalidCredentialsException;
import com.example.cryptosim.exception.UserNotFoundException;
import com.example.cryptosim.model.Role;
import com.example.cryptosim.model.User;
import com.example.cryptosim.repository.RoleRepository;
import com.example.cryptosim.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public User authenticateUser(String username, String password) throws InvalidCredentialsException {
        log.info("Authenticating user with username: {}", username);
        User user = userRepository.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Failed login attempt for user: {}", username);
            throw new InvalidCredentialsException("Invalid credentials. Please check your username and password.");
        }
        log.info("User {} successfully authenticated.", username);
        return user;
    }

    public List<User> getAllUsers() throws UserNotFoundException {
        log.info("Retrieving all users from the database.");
        Role userRole = roleRepository.findByRoleName("USER");
        List<User> users = userRepository.findByRole(userRole);

        if (users == null || users.isEmpty()) {
            log.error("No users found in the database");
            throw new UserNotFoundException("No users found");
        }

        log.info("Successfully retrieved {} users", users.size());
        return users;
    }
}