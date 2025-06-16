package site.arookieofc.service.impl;

import dev.langchain4j.service.TokenStream;

public interface Bot {
    TokenStream chat(String message);
}
