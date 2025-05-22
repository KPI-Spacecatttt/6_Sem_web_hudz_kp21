package com.example.lab4.Binance;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.example.lab4.protobuf.PriceUpdateClass;

import jakarta.annotation.PreDestroy;

@Component
public class TradeWebSocketHandler extends BinaryWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    // Карта для зберігання черг повідомлень для кожної сесії
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<BinaryMessage>> sessionQueues = new ConcurrentHashMap<>();
    // Пул потоків для обробки черг (по одному потоку на сесію або загальний)
    // Використовуємо кешований пул, який створює нові потоки за потреби та
    // переробляє існуючі
    private final ExecutorService messageSenderExecutor = Executors.newCachedThreadPool();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        // Створюємо нову чергу для нової сесії
        ConcurrentLinkedQueue<BinaryMessage> queue = new ConcurrentLinkedQueue<>();
        sessionQueues.put(session.getId(), queue);
        // Запускаємо окремий потік для обробки повідомлень для цієї сесії
        messageSenderExecutor.submit(() -> processSessionMessages(session, queue));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        // При закритті сесії видаляємо її чергу
        sessionQueues.remove(session.getId());
        // Додатково: можливо, потрібно якось "зупинити" потік, який обробляв цю чергу,
        // але для `Executors.newCachedThreadPool` це не критично.
    }

    public void broadcastPriceUpdate(PriceUpdateClass.PriceUpdate protoUpdate) {
        byte[] bytes = protoUpdate.toByteArray();
        BinaryMessage message = new BinaryMessage(bytes);
        for (WebSocketSession session : sessions) {
            // Додаємо повідомлення до черги відповідної сесії
            ConcurrentLinkedQueue<BinaryMessage> queue = sessionQueues.get(session.getId());
            if (queue != null) {
                queue.offer(message); // Додаємо повідомлення в кінець черги
            }
        }
    }

    // Метод, який буде виконуватися в окремому потоці для кожної сесії
    private void processSessionMessages(WebSocketSession session, ConcurrentLinkedQueue<BinaryMessage> queue) {
        while (session.isOpen()) { // Обробляємо повідомлення, поки сесія відкрита
            BinaryMessage message = queue.poll(); // Витягуємо повідомлення з черги
            if (message != null) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    System.err.println("Failed to send message for session " + session.getId() + ": " + e.getMessage());
                    // Можливо, потрібно закрити сесію або видалити її, якщо виникла критична
                    // помилка
                    if (!session.isOpen()) {
                        sessions.remove(session);
                        sessionQueues.remove(session.getId());
                        break; // Вийти з циклу, якщо сесія закрита
                    }
                } catch (IllegalStateException e) {
                    // Це наша помилка "BINARY_PARTIAL_WRITING" або інші стани
                    System.err.println(
                            "State error sending message for session " + session.getId() + ": " + e.getMessage());
                    // У цьому випадку можна спробувати відправити повідомлення знову, але краще
                    // покласти його назад у чергу, щоб не блокувати інші повідомлення.
                    // Однак, `queue.poll()` вже видалив його.
                    // Більш надійний підхід - це обробка помилок та перевірка станів.
                    // З використанням черги та одного потоку на сесію, ця помилка має бути
                    // виключена,
                    // якщо тільки немає якихось специфічних умов самого WebSocket-з'єднання.
                    // Якщо помилка все ще виникає, це може вказувати на проблему з самим
                    // WebSocket-фреймворком
                    // або дуже повільну клієнтську сторону.
                }
            } else {
                // Якщо повідомлень немає, чекаємо трохи, щоб уникнути зайвого завантаження CPU
                try {
                    Thread.sleep(20); // Невеликий таймаут
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Message sender thread interrupted for session " + session.getId());
                    break;
                }
            }
        }
        System.out.println("Message sender thread stopped for session " + session.getId());
    }

    @PreDestroy
    public void shutdownExecutor() {
        messageSenderExecutor.shutdown(); // Правильно завершуємо пул потоків
    }
}