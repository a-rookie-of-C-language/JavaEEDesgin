package site.arookieofc.service.impl;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

public interface Bot {
    @SystemMessage("请使用中文,编码请使用UTF-8")
    TokenStream chat(String message);
}
