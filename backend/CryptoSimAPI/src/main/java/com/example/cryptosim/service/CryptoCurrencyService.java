package com.example.cryptosim.service;

import com.example.cryptosim.model.CryptoCurrency;
import com.example.cryptosim.repository.CryptoCurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoCurrencyService {
    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    @Autowired
    public CryptoCurrencyService(CryptoCurrencyRepository cryptoCurrencyRepository) {
        this.cryptoCurrencyRepository = cryptoCurrencyRepository;
    }

    public List<CryptoCurrency> getAllCryptos() {
        return cryptoCurrencyRepository.findAll();
    }
}
