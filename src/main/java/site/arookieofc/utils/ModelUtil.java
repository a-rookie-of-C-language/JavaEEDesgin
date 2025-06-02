package site.arookieofc.utils;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import site.arookieofc.annotation.config.Config;

public class ModelUtil {
    @Config("ai.url")
    private static String URL;
    @Config("ai.name")
    private static String MODEL_NAME;
    
    private static ChatLanguageModel languageModel;
    private static StreamingChatLanguageModel streamingLanguageModel;

    public static ChatLanguageModel getLanguageModel(){
        if (languageModel == null) {
            languageModel = OllamaChatModel.builder()
                    .baseUrl(URL)
                    .modelName(MODEL_NAME)
                    .build();
        }
        return languageModel;
    }

    public static StreamingChatLanguageModel getStreamingLanguageModel(){
        if (streamingLanguageModel == null) {
            streamingLanguageModel = OllamaStreamingChatModel.builder()
                    .baseUrl(URL)
                    .modelName(MODEL_NAME)
                    .build();
        }
        return streamingLanguageModel;
    }
}
