package com.example.cryptosim.repository;

import com.example.cryptosim.model.User;
import com.example.cryptosim.model.UserCryptoAsset;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserCryptoAssetRepository extends JpaRepository<UserCryptoAsset, Long> {
    // You can define custom queries here if needed
    Optional<UserCryptoAsset> findByUserAndCryptoCurrencyName(User user, String cryptoName);

    List<UserCryptoAsset> findByUser(User user);

    // Fetch by username and crypto name
    @Query("SELECT uca FROM UserCryptoAsset uca WHERE uca.user.username = :username AND uca.cryptoCurrency.pair = :cryptoPair")
    Optional<UserCryptoAsset> findByUsernameAndCryptoCurrencyName(@Param("username") String username,
                                                                  @Param("cryptoPair") String cryptoPair);

    @Query("SELECT uca FROM UserCryptoAsset uca WHERE uca.user.username = :username")
    List<UserCryptoAsset> findByUsername(@Param("username") String username);

    @Modifying
    @Transactional
    @Query("DELETE FROM UserCryptoAsset uca WHERE uca.user.username = :username")
    void deleteByUsername(@Param("username") String username);
}
