package com.example.cryptosim.repository;

import com.example.cryptosim.model.Transaction;
import com.example.cryptosim.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    @Transactional
    void deleteByUserUsername(String username);
}
