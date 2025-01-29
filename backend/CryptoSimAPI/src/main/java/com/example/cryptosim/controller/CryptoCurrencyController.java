package com.example.cryptosim.controller;

import com.example.cryptosim.model.CryptoCurrency;
import com.example.cryptosim.service.CryptoCurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cryptocurrencies")
public class CryptoCurrencyController {
    private final CryptoCurrencyService cryptoCurrencyService;

    @Autowired
    public CryptoCurrencyController(CryptoCurrencyService cryptoCurrencyService) {
        this.cryptoCurrencyService = cryptoCurrencyService;
    }

    @GetMapping("/get-all-cryptocurrencies")
    public List<CryptoCurrency> getAllCryptocurrencies() {
        return cryptoCurrencyService.getAllCryptos();
    }
}
