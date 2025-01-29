package com.example.cryptosim.websocket;

import jakarta.websocket.Session;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;

@Component
@ServerEndpoint("/cryptocurrencies")
public class BackendToFrontendWebSocketHandler {

    private static Map<String, String> cryptoPriceMap = Collections.synchronizedMap(new TreeMap<>());
    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    // On connection, add the new client session to the set
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("New client connected: " + session.getId());

        broadcastAllPriceUpdates();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("Message received from client: " + message);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        clients.remove(session);
        System.out.println("Client disconnected: " + session.getId() + " Reason: " + closeReason.getReasonPhrase());
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (Session clientSession : clients) {
                try {
                    RemoteEndpoint.Basic remote = clientSession.getBasicRemote();
                    remote.sendText(message); // Send the message to the client
                } catch (IOException e) {
                    System.err.println("Error broadcasting to client " + clientSession.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    // Broadcast all price updates to all clients
    private void broadcastAllPriceUpdates() {
        synchronized (cryptoPriceMap) {
            for (String message : cryptoPriceMap.values()) {
                broadcast(message); // Send all stored price updates
            }
        }
    }

    // Update the price for a specific cryptocurrency pair and broadcast it
    public static void addNewPriceUpdate(String pair, String priceUpdateMessage) {
        // Store the new price update for the cryptocurrency pair in the TreeMap
        cryptoPriceMap.put(pair, priceUpdateMessage); // Automatically sorted by key

        BackendToFrontendWebSocketHandler handler = new BackendToFrontendWebSocketHandler();
        handler.broadcast(priceUpdateMessage);
    }

    public static String getPriceUpdateForCrypto(String cryptoName) {
        return cryptoPriceMap.get(cryptoName);
    }

    public static void printAllPriceUpdates() {
        synchronized (cryptoPriceMap) {
            // Print all entries in the map
            cryptoPriceMap.forEach((key, value) -> {
                System.out.println("Symbol: " + key + " -> " + value);
            });
        }
    }
}
