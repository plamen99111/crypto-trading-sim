package com.example.cryptosim.controller;

import com.example.cryptosim.model.Transaction;
import com.example.cryptosim.service.TransactionService;
import com.example.cryptosim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    // Endpoint to display the balance of a user by username
    @GetMapping("/balance")
    public BigDecimal getUserBalance(@RequestParam String username) {
        try {
            return userService.getUserBalance(username);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching balance: " + e.getMessage());
        }
    }

    @GetMapping("/transactions/{username}")
    public List<Transaction> getAllTransactions(@PathVariable String username) {
        return transactionService.getAllTransactions(username);
    }

    @GetMapping("/reset/{username}")
    public ResponseEntity<String> reset(@PathVariable String username) {
        userService.resetAccount(username);
        return ResponseEntity.ok("Account for user " + username + " has been reset.");
    }

}
