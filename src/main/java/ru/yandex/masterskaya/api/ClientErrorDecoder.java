package ru.yandex.masterskaya.api;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.masterskaya.exception.BadRequestException;
import ru.yandex.masterskaya.exception.NotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        log.error("Feign error occurred while calling method: {}. HTTP status: {}, reason: {}",
                methodKey, response.status(), response.reason());

        String responseBody = "";
        if (response.body() != null) {

            try (Response.Body body = response.body()) {
                byte[] bodyData = body.asInputStream().readAllBytes();
                responseBody = new String(bodyData, StandardCharsets.UTF_8);
                log.error("Response body: {}", responseBody);
            } catch (IOException e) {
                log.warn("Unable to read response body from Feign response", e);
            }
        }


        return switch (response.status()) {
            case 404 -> NotFoundException.builder()
                    .message(!responseBody.isBlank() ?
                            responseBody : "Entity not found (no details provided)")
                    .build();
            case 400 -> BadRequestException.builder()
                    .message(!responseBody.isBlank() ?
                            "Bad Request: " + responseBody : "Bad Request (no details provided)")
                    .build();
            default -> new RuntimeException(
                    String.format("Unknown error: HTTP %d, reason: %s, body: %s",
                            response.status(), response.reason(), responseBody)
            );
        };
    }
}

