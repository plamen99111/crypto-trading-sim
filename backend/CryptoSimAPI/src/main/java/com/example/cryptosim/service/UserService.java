package com.example.cryptosim.service;

import com.example.cryptosim.model.User;
import com.example.cryptosim.repository.TransactionRepository;
import com.example.cryptosim.repository.UserCryptoAssetRepository;
import com.example.cryptosim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCryptoAssetRepository userCryptoAssetsRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Method to get the balance of a user by username
    public BigDecimal getUserBalance(String username) {
        Optional<BigDecimal> balance = userRepository.findBalanceByUsername(username);
        if (balance.isPresent()) {
            return balance.get();
        } else {
            throw new RuntimeException("User not found");
        }
    }

    // Method to reset the user's account
    public void resetAccount(String username) {
        // Ensure the repository delete method is called correctly
        transactionRepository.deleteByUserUsername(username);  // No result expected
        userCryptoAssetsRepository.deleteByUsername(username); // No result expected
        userRepository.resetUserBalance(username, User.DEFAULT_ACCOUNT_BALANCE);  // Reset user balance
    }
}
