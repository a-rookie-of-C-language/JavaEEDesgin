import dev.langchain4j.model.chat.ChatLanguageModel;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.utils.ModelUtil;

public class ModelTest {
    public static void main(String[] args) {
        ConfigProcessor.injectStaticFields(ModelUtil.class);
        ChatLanguageModel model = ModelUtil.getLanguageModel();
        System.out.println(model.chat("你是谁?"));
    }
}
