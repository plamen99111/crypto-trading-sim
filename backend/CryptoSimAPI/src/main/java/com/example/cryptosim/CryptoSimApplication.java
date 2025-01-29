package com.example.cryptosim;

import com.example.cryptosim.repository.CryptoCurrencyRepository;
import com.example.cryptosim.websocket.BackendToFrontendWebSocketHandler;
import com.example.cryptosim.websocket.KrakenWebSocketClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CryptoSimApplication implements CommandLineRunner {

    private final BackendToFrontendWebSocketHandler webSocketHandler;
    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    // Constructor injection for BackendToFrontendWebSocketHandler
    public CryptoSimApplication(BackendToFrontendWebSocketHandler webSocketHandler,
                                CryptoCurrencyRepository cryptoCurrencyRepository) {
        this.webSocketHandler = webSocketHandler;
        this.cryptoCurrencyRepository = cryptoCurrencyRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(CryptoSimApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        KrakenWebSocketClient krakenClient = new KrakenWebSocketClient(webSocketHandler, cryptoCurrencyRepository);
        krakenClient.connect();
    }
}
