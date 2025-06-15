package site.arookieofc.service;

import reactor.core.publisher.Flux;

public interface AiService {
    Flux<String> chatStream(String message);
}