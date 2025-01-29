package com.example.cryptosim.repository;

import com.example.cryptosim.model.CryptoCurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;


public interface CryptoCurrencyRepository extends JpaRepository<CryptoCurrency, Long> {

    Logger logger = LoggerFactory.getLogger(CryptoCurrencyRepository.class);

    @Query("SELECT c.pair FROM CryptoCurrency c ORDER BY c.id ASC")
    Set<String> findAllPairs(); // This fetches unique pairs only

    Optional<CryptoCurrency> findByName(String name);

    Optional<CryptoCurrency> findByPair(String pair);
}
