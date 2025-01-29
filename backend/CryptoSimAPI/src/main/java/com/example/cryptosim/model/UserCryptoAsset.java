package com.example.cryptosim.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "user_crypto_assets")
public class UserCryptoAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "crypto_name", referencedColumnName = "name", nullable = false)
    private CryptoCurrency cryptoCurrency;

    private BigDecimal quantity; // Quantity of cryptocurrency owned by the user

    public UserCryptoAsset(User user, CryptoCurrency cryptoCurrency, BigDecimal quantity) {
        this.user = user;
        this.cryptoCurrency = cryptoCurrency;
        this.quantity = quantity;
    }

    public UserCryptoAsset() {
        this.user = null;
        this.cryptoCurrency = null;
        this.quantity = null;
    }
}
