package site.arookieofc.service.impl;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.AiService;
import site.arookieofc.utils.ModelUtil;

@Slf4j
@Component
public class AiServiceImpl implements AiService {

    @Override
    public Flux<String> chatStream(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
            StreamingChatLanguageModel model = ModelUtil.getStreamingLanguageModel();
            return Flux.create(sink -> model.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    sink.next(partialResponse);
                }
                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    sink.complete();
                }
                @Override
                public void onError(Throwable error) {
                    sink.error(error);
                }
            }));
    }
}