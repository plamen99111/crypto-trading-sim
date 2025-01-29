package com.example.cryptosim.repository;

import com.example.cryptosim.model.Role;
import com.example.cryptosim.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    Optional<User> findOptionalByUsername(String username);

    @Query("SELECT u.balance FROM User u WHERE u.username = :username")
    Optional<BigDecimal> findBalanceByUsername(@Param("username") String username);

    List<User> findByRole(Role role);

    boolean existsById(Long id);

    Optional<User> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.balance = :defaultBalance WHERE u.username = :username")
    void resetUserBalance(@Param("username") String username, @Param("defaultBalance") BigDecimal defaultBalance);

}