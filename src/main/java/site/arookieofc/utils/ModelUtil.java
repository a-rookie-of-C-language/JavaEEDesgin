package site.arookieofc.utils;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.Getter;
import site.arookieofc.annotation.config.Config;

public class ModelUtil {
    @Config("ai.url")
    private static String URL;
    @Config("ai.name")
    private static String modelName;
    
    private static ChatLanguageModel languageModel;

    public static ChatLanguageModel getLanguageModel(){
        if (languageModel == null) {
            languageModel = OllamaChatModel.builder()
                    .baseUrl(URL)
                    .modelName(modelName)
                    .build();
        }
        return languageModel;
    }

}
