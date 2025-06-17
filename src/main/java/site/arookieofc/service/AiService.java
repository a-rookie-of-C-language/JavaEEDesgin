package site.arookieofc.service;

import dev.langchain4j.service.TokenStream;

public interface AiService {
    TokenStream chatStream(String message);
}