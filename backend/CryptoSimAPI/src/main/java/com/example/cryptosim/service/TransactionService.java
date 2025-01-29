package com.example.cryptosim.service;

import com.example.cryptosim.model.Transaction;
import com.example.cryptosim.repository.TransactionRepository;
import com.example.cryptosim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Transaction> getAllTransactions(String username) {
        // Fetch the user by username
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        // Fetch transactions for that user ordered by transactionDate in descending order
        return transactionRepository.findByUserOrderByTransactionDateDesc(user);
    }
}

