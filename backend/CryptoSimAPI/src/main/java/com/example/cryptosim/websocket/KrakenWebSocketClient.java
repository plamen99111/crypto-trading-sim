package com.example.cryptosim.websocket;

import com.example.cryptosim.repository.CryptoCurrencyRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ClientEndpoint
public class KrakenWebSocketClient {

    private Session session;
    private final BackendToFrontendWebSocketHandler handler;
    private final CryptoCurrencyRepository cryptoCurrencyRepository;

    // Constructor accepts an existing handler
    public KrakenWebSocketClient(BackendToFrontendWebSocketHandler handler,
                                 CryptoCurrencyRepository cryptoCurrencyRepository) {
        this.handler = handler;
        this.cryptoCurrencyRepository = cryptoCurrencyRepository;
    }

    // Connect to the WebSocket URI
    public void connect() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, URI.create("wss://ws.kraken.com/v2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;

        // Fetch only the unique pairs from the database
        Set<String> pairs = cryptoCurrencyRepository.findAllPairs(); // Custom query to fetch only pairs

        // Convert the Set to JSON array format for Kraken WebSocket API
        String pairsJsonArray =
            pairs.stream().map(pair -> "\"" + pair + "\"").collect(Collectors.joining(", ", "[", "]"));

        // Subscription message to Kraken WebSocket API
        String subscriptionMessage = String.format("""
            {
                "method": "subscribe",
                "params": {
                    "channel": "ticker",
                    "symbol": %s
                }
            }
            """, pairsJsonArray);

        // Send the subscription message
        sendMessage(subscriptionMessage);
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            // Parse and process Kraken's response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(message);

            if (rootNode.has("channel") && "heartbeat".equals(rootNode.get("channel").asText())) {
                return;
            }
            // Filter and process relevant data (exclude subscription responses)
            if (rootNode.has("data") && rootNode.get("data").isArray()) {
                JsonNode dataNode = rootNode.get("data").get(0);
                if (dataNode != null) {
                    String symbol = dataNode.has("symbol") ? dataNode.get("symbol").asText() : null;
                    BigDecimal bidPrice =
                        dataNode.has("bid") ? new BigDecimal(dataNode.get("bid").asText()) : BigDecimal.ZERO;
                    BigDecimal bidQuantity =
                        dataNode.has("bid_qty") ? new BigDecimal(dataNode.get("bid_qty").asText()) : BigDecimal.ZERO;
                    BigDecimal askPrice =
                        dataNode.has("ask") ? new BigDecimal(dataNode.get("ask").asText()) : BigDecimal.ZERO;
                    BigDecimal askQuantity =
                        dataNode.has("ask_qty") ? new BigDecimal(dataNode.get("ask_qty").asText()) : BigDecimal.ZERO;


                    if (symbol != null && bidPrice.compareTo(BigDecimal.ZERO) != 0 &&
                        bidQuantity.compareTo(BigDecimal.ZERO) != 0 && askPrice.compareTo(BigDecimal.ZERO) != 0 &&
                        askQuantity.compareTo(BigDecimal.ZERO) != 0) {

                        // Prepare a message with relevant data
                        String priceUpdateMessage = String.format(Locale.US,
                            "{\"crypto\": \"%s\", \"buyingPrice\": %.2f, \"buyingMaxQuantity\": %.8f, \"sellingPrice\": %.2f, \"sellingMaxQuantity\": %.8f}",
                            symbol, bidPrice.setScale(2, RoundingMode.HALF_UP), bidQuantity,
                            askPrice.setScale(2, RoundingMode.HALF_UP), askQuantity);

                        BackendToFrontendWebSocketHandler.addNewPriceUpdate(symbol, priceUpdateMessage);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {

    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    // Send a message to Kraken API
    private void sendMessage(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message); // Send the message
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
