package site.arookieofc.service;

import java.util.List;
import java.util.Map;

public interface AiService {

    String chat(String message);

    String chatWithHistory(List<Map<String, String>> messages);

    boolean isAvailable();

    Map<String, String> getModelInfo();
}