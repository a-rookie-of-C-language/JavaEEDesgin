package site.arookieofc.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface AiService {
    Flux<String> chatStream(String message);
}