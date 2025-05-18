package com.example.lab4.Binance;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.binance.connector.client.WebSocketStreamClient;
import com.binance.connector.client.impl.WebSocketStreamClientImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class BinanceWebSocketService {
    private final BinanceProperties binanceProperties;
    private final WebSocketStreamClient client;
    private final SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BinanceWebSocketService(BinanceProperties binanceProperties, SimpMessagingTemplate messagingTemplate) {
        this.binanceProperties = binanceProperties;
        this.messagingTemplate = messagingTemplate;
        this.client = new WebSocketStreamClientImpl();
    }

    @PostConstruct
    public void init() {
        List<String> currencies = binanceProperties.getCurrencies();
        if (currencies == null || currencies.isEmpty()) {
            return;
        }
        currencies.forEach(this::subscribeToTradeStream);
    }

    @PreDestroy
    public void cleanup() {
        client.closeAllConnections();
    }

    private void subscribeToTradeStream(String symbol) {
        String lowerCaseSymbol = symbol.toLowerCase();

        client.tradeStream(lowerCaseSymbol, event -> {
            try {
                JsonNode tradeEvent = objectMapper.readTree(event);

                if (tradeEvent.has("e") && "trade".equals(tradeEvent.get("e").asText()) &&
                        tradeEvent.has("s") && tradeEvent.has("p")) {

                    String receivedSymbol = tradeEvent.get("s").asText();
                    String price = tradeEvent.get("p").asText();

                    // Створюємо об'єкт Protobuf повідомлення, використовуючи згенеровані класи
                    com.example.lab4.protobuf.PriceUpdateClass.PriceUpdate protoUpdate = com.example.lab4.protobuf.PriceUpdateClass.PriceUpdate
                            .newBuilder()
                            .setSymbol(receivedSymbol) // Встановлюємо поля
                            .setPrice(price)
                            .build(); // Будуємо повідомлення

                    // Серіалізуємо Protobuf повідомлення у масив байтів
                    byte[] protoBytes = protoUpdate.toByteArray();

                    // Надсилаємо масив байтів через STOMP брокер на тему "/topic/prices"
                    // Spring Messaging автоматично визначить, що це бінарні дані (byte[])
                    messagingTemplate.convertAndSend("/topic/prices", protoBytes);

                } else {
                    System.out.println(String.format("Received non-trade event or unexpected format for {}: {}",
                            lowerCaseSymbol, event));
                }
            } catch (Exception e) {
                System.out.println(
                        String.format("Error processing Binance trade event for {}: {}", lowerCaseSymbol, event, e));
            }
        });
    }
}