package site.arookieofc.service.impl;

import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.annotation.validation.Need;
import site.arookieofc.service.AiService;
import site.arookieofc.utils.ModelUtil;

@Slf4j
@Component
public class AiServiceImpl implements AiService {

    @Override
    public TokenStream chatStream(@Need String message) {
        boolean needsMcp = shouldUseMcp(message);
        log.info("needsMcp={}", needsMcp);
        
        if (needsMcp) {
            log.info("使用MCP增强AI服务处理请求");
            return chatWithMcp(message);
        } else {
            log.info("MCP可用状态: {}", ModelUtil.isMcpAvailable());
            log.info("使用普通AI服务处理请求");
            return chatWithoutMcp(message);
        }
    }

    // 判断是否需要使用MCP（可以根据消息内容、关键词等判断）
    private boolean shouldUseMcp(String message) {
        // 检查消息是否包含需要工具调用的关键词
        String[] mcpKeywords = {"学生", "老师", "班级", "查询", "添加", "删除", "修改", "管理"};
        String lowerMessage = message.toLowerCase();
        for (String keyword : mcpKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // 使用MCP增强的AI服务
    private TokenStream chatWithMcp(String message) {
        try {
            Bot bot = ModelUtil.getBotWithMcp();
            if (bot == null) {
                log.warn("MCP Bot获取失败，降级到普通AI");
                return chatWithoutMcp(message);
            }

            log.info("开始调用MCP AI服务");
            return bot.chat(message);
        } catch (Exception e) {
            log.error("MCP AI服务调用失败，降级到普通AI: {}", e.getMessage(), e);
            return chatWithoutMcp(message);
        }
    }

    // 普通AI服务（不使用MCP）
    private TokenStream chatWithoutMcp(String message) {
        try {
            Bot bot = ModelUtil.getBotWithoutMcp();
            log.info("开始调用普通AI服务");
            return bot.chat(message);
        } catch (Exception e) {
            log.error("普通AI服务调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("AI服务不可用", e);
        }
    }
}