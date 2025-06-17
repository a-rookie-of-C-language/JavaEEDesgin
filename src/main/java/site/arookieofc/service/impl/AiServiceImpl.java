package site.arookieofc.service.impl;

import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.service.AiService;
import site.arookieofc.utils.ModelUtil;

import java.util.Optional;

@Slf4j
@Component
public class AiServiceImpl implements AiService {

    @Override
    public TokenStream chatStream(@Need String message) {
        return shouldUseMcp(message)
                ? chatWithMcp(message)
                : chatWithoutMcp(message);
    }

    private boolean shouldUseMcp(String message) {
        String[] mcpKeywords = {"学生", "老师", "班级", "查询", "添加", "删除", "修改", "管理"};
        String lowerMessage = message.toLowerCase();
        for (String keyword : mcpKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private TokenStream chatWithMcp(String message) {
        Optional<Bot> bot = Optional.ofNullable(ModelUtil.getBotWithMcp());
        return bot
                .orElseThrow(() -> new RuntimeException("bot is null"))
                .chat(message);
    }

    private TokenStream chatWithoutMcp(String message) {
        Bot bot = ModelUtil.getBotWithoutMcp();
        return bot.chat(message);
    }
}