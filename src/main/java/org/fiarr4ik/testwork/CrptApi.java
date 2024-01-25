package org.fiarr4ik.testwork;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final int requestLimit;
    private final long timeInterval;
    private long lastRequestTime;
    private int requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        // Инициализация клиента, объекта для сериализации JSON и других полей
        this.httpClient = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiUrl = "https://ismp.crpt.ru/api/v3/lk/documents/create";
        this.requestLimit = requestLimit;
        this.timeInterval = timeUnit.toMillis(1);
        this.lastRequestTime = System.currentTimeMillis();
        this.requestCount = 0;
    }

    public synchronized void createDocument(Document document, String signature) {
        checkRequestLimit();

        try {
            String jsonBody = objectMapper.writeValueAsString(document);

            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody);

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(requestBody)
                    .addHeader("Signature", signature)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Ошибка: " + response.code() +  response.message());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        updateRequestCount();
    }

    private void checkRequestLimit() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime >= timeInterval) {
            requestCount = 0;
            lastRequestTime = currentTime;
        }

        if (requestCount >= requestLimit) {
            try {
                Thread.sleep(timeInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateRequestCount() {
        requestCount++;
    }

    static class Document {
    }
}