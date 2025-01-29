package com.example.cryptosim.controller;

import com.example.cryptosim.model.User;
import com.example.cryptosim.model.UserCryptoAsset;
import com.example.cryptosim.service.UserCryptoAssetService;
import com.example.cryptosim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/user/crypto")
public class UserCryptoAssetController {

    @Autowired
    private UserCryptoAssetService userCryptoAssetService;

    @Autowired
    private UserService userService;

    @GetMapping("/assets")
    public List<UserCryptoAsset> getAllAssetsForUser(@RequestParam String username) {
        try {
            // Call the new service method
            return userCryptoAssetService.getAllAssetsForUserByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching assets for user " + username + ": " + e.getMessage());
        }
    }


    // Endpoint for buying cryptocurrency
    @PostMapping("/buy")
    public String buyCrypto(@RequestParam String username, @RequestParam String cryptoPair,
                            @RequestParam BigDecimal quantity, @RequestParam BigDecimal price,
                            @RequestParam BigDecimal maxBuyingQuantity) {

        try {
            // Call the service method to buy cryptocurrency
            userCryptoAssetService.buyCryptoAsset(username, cryptoPair, quantity, price, maxBuyingQuantity);
            return "Successfully bought " + quantity + " of " + cryptoPair + " at price " + price;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Endpoint for selling cryptocurrency
    @PostMapping("/sell")
    public String sellCrypto(@RequestParam String username, @RequestParam String cryptoPair,
                             @RequestParam BigDecimal quantity, @RequestParam BigDecimal price,
                             @RequestParam BigDecimal maxSellingQuantity) {

        try {
            userCryptoAssetService.sellCryptoAsset(username, cryptoPair, quantity, price, maxSellingQuantity);
            return "Successfully sold " + quantity + " of " + cryptoPair + " at price " + price;

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
