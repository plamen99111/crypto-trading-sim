package com.example.cryptosim.service;

import com.example.cryptosim.model.Transaction;
import com.example.cryptosim.model.User;
import com.example.cryptosim.model.UserCryptoAsset;
import com.example.cryptosim.model.CryptoCurrency;
import com.example.cryptosim.repository.TransactionRepository;
import com.example.cryptosim.repository.UserCryptoAssetRepository;
import com.example.cryptosim.repository.CryptoCurrencyRepository;
import com.example.cryptosim.repository.UserRepository;
import com.example.cryptosim.websocket.BackendToFrontendWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class UserCryptoAssetService {

    @Autowired
    private UserCryptoAssetRepository userCryptoAssetRepository;

    @Autowired
    private CryptoCurrencyRepository cryptoCurrencyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<UserCryptoAsset> getAllAssetsForUserByUsername(String username) {
        return userCryptoAssetRepository.findByUsername(username);
    }

    public UserCryptoAsset buyCryptoAsset(String username, String cryptoPair, BigDecimal quantity, BigDecimal price,
                                          BigDecimal maxBuyingQuantity) {

        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Requested quantity is negative number.");
        }
        // Fetch the CryptoCurrency by its name
        Optional<CryptoCurrency> cryptoCurrencyOpt = cryptoCurrencyRepository.findByPair(cryptoPair);
        if (!cryptoCurrencyOpt.isPresent()) {
            throw new IllegalArgumentException("Cryptocurrency not found: " + cryptoPair);
        }
        CryptoCurrency cryptoCurrency = cryptoCurrencyOpt.get();

        // Fetch the User by username
        User user = userRepository.findOptionalByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Get the real-time Kraken data (from WebSocket updates) for this cryptocurrency
        String krakenData = BackendToFrontendWebSocketHandler.getPriceUpdateForCrypto(cryptoPair);
        if (krakenData == null || krakenData.isEmpty()) {
            throw new IllegalArgumentException("No real-time data available for " + cryptoPair);
        }

        // Extract the max available buying quantity from Kraken's data
        BigDecimal availableBuyingQuantity = extractPropertyFromMessage("buyingMaxQuantity", krakenData);

        // Ensure the requested quantity does not exceed Kraken's available quantity
        if (quantity.compareTo(availableBuyingQuantity) > 0) {
            throw new IllegalArgumentException("Requested quantity exceeds the available quantity to buy from Kraken.");
        }

        // Ensure the requested quantity is within the provided max buying limit
        if (quantity.compareTo(maxBuyingQuantity) > 0) {
            throw new IllegalArgumentException("Requested quantity exceeds the maximum allowed buying quantity.");
        }

        // Calculate the total cost for the purchase
        BigDecimal totalCost = price.multiply(quantity);

        // Check if the user has enough balance to buy the cryptocurrency
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient balance to complete the purchase");
        }

        // Deduct the total cost from the user's balance
        user.setBalance(user.getBalance().subtract(totalCost));

        // Find the existing user crypto asset by username and crypto name
        Optional<UserCryptoAsset> existingAsset =
            userCryptoAssetRepository.findByUsernameAndCryptoCurrencyName(username, cryptoPair);

        if (existingAsset.isPresent()) {
            // If the asset exists, add the quantity to the existing one
            UserCryptoAsset asset = existingAsset.get();
            asset.setQuantity(asset.getQuantity().add(quantity)); // Add the quantity for buying
            userCryptoAssetRepository.save(asset);
        } else {
            // If the asset doesn't exist, create a new entry for the user
            UserCryptoAsset newAsset = new UserCryptoAsset(user, cryptoCurrency, quantity);
            userCryptoAssetRepository.save(newAsset);
        }

        Transaction transaction = new Transaction();
        transaction.setCryptoName(cryptoCurrency.getName());
        transaction.setCryptoSymbol(cryptoCurrency.getSymbol());
        transaction.setQuantity(quantity);
        transaction.setPrice(price);
        transaction.setType("BUY");
        transaction.setUser(user);
        transactionRepository.save(transaction);

        // Return the updated asset
        return userCryptoAssetRepository.findByUsernameAndCryptoCurrencyName(username, cryptoPair).get();
    }


    // Method for selling cryptocurrency (add balance)
    public UserCryptoAsset sellCryptoAsset(String username, String cryptoPair, BigDecimal quantity, BigDecimal price,
                                           BigDecimal maxSellingQuantity) {
        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Requested quantity is negative number.");
        }

        // Fetch the CryptoCurrency by its name
        Optional<CryptoCurrency> cryptoCurrencyOpt = cryptoCurrencyRepository.findByPair(cryptoPair);
        if (!cryptoCurrencyOpt.isPresent()) {
            throw new IllegalArgumentException("Cryptocurrency not found: " + cryptoPair);
        }
        CryptoCurrency cryptoCurrency = cryptoCurrencyOpt.get();

        // Fetch the User by username
        User user = userRepository.findOptionalByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Find the existing user crypto asset by username and crypto name
        Optional<UserCryptoAsset> existingAsset =
            userCryptoAssetRepository.findByUsernameAndCryptoCurrencyName(username, cryptoPair);

        if (existingAsset.isPresent()) {
            // Check if the requested quantity exceeds the max selling quantity
            String krakenData = BackendToFrontendWebSocketHandler.getPriceUpdateForCrypto(cryptoPair);
            if (krakenData == null || krakenData.isEmpty()) {
                throw new IllegalArgumentException("No real-time data available for " + cryptoPair);
            }

            // Extract the max available buying quantity from Kraken's data
            BigDecimal availableSellingQuantity = extractPropertyFromMessage("sellingMaxQuantity", krakenData);

            // Ensure the requested quantity does not exceed Kraken's available quantity
            if (quantity.compareTo(availableSellingQuantity) > 0) {
                throw new IllegalArgumentException(
                    "Requested quantity exceeds the available quantity to sell from Kraken.");
            }

            BigDecimal availableQuantity = existingAsset.get().getQuantity();
            if (quantity.compareTo(availableQuantity) > 0) {
                throw new IllegalArgumentException(
                    "Requested quantity exceeds the available quantity in your assets to sell.");
            }

            if (quantity.compareTo(maxSellingQuantity) > 0) {
                throw new IllegalArgumentException("Requested quantity exceeds the maximum allowed selling quantity.");
            }

            // Subtract the quantity from the user's holdings
            UserCryptoAsset asset = existingAsset.get();
            BigDecimal newQuantity = asset.getQuantity().subtract(quantity);

            // Update the quantity
            asset.setQuantity(newQuantity);

            // Calculate the total proceeds from the sale
            BigDecimal totalProceeds = price.multiply(quantity);

            // Add the proceeds to the user's balance
            user.setBalance(user.getBalance().add(totalProceeds));

            userCryptoAssetRepository.save(asset);

            Transaction transaction = new Transaction();
            transaction.setCryptoName(cryptoCurrency.getName());
            transaction.setCryptoSymbol(cryptoCurrency.getSymbol());
            transaction.setQuantity(quantity);
            transaction.setPrice(price);
            transaction.setType("SELL");
            transaction.setUser(user);
            transactionRepository.save(transaction);

        } else {
            throw new IllegalArgumentException("User does not own the cryptocurrency: " + cryptoPair);
        }

        return userCryptoAssetRepository.findByUsernameAndCryptoCurrencyName(username, cryptoPair).get();
    }

    // Helper method to extract the buyingMaxQuantity from the priceUpdateMessage
    private BigDecimal extractPropertyFromMessage(String propertyString, String priceUpdateMessage) {
        try {
            String[] parts = priceUpdateMessage.split(",");
            for (String part : parts) {
                if (part.contains(propertyString)) {
                    // Extract the buyingMaxQuantity value
                    String quantityPart = part.split(":")[1].trim();
                    quantityPart = quantityPart.replaceAll("[^0-9.]", ""); // Remove non-numeric characters
                    return new BigDecimal(quantityPart);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO; // Default to zero if parsing fails
    }
}
