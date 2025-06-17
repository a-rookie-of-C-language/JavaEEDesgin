package site.arookieofc.utils;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import lombok.Getter;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.service.impl.Bot;
import java.time.Duration;
import java.util.List;

@Component
public class ModelUtil {
    @Config("ai.url")
    private static String URL;
    @Config("ai.name")
    private static String MODEL_NAME;
    @Config(value = "ai.timeout.request", defaultValue = "60000")
    private static int REQUEST_TIMEOUT;

    private static StreamingChatModel ollamaStreamingChatModel;
    private static ToolProvider mcpToolProvider;
    private static Bot botWithMcp;
    private static Bot botWithoutMcp;

    @Getter
    private static boolean mcpAvailable = false;

    public static StreamingChatModel getBot(){
        if (ollamaStreamingChatModel == null) {
            ollamaStreamingChatModel = OllamaStreamingChatModel.builder()
                    .baseUrl(URL)
                    .modelName(MODEL_NAME)
                    .timeout(Duration.ofMillis(REQUEST_TIMEOUT))
                    .logRequests(true)
                    .logResponses(true)
                    .build();
        }
        return ollamaStreamingChatModel;
    }

    public static Bot getBotWithMcp() {
        if (botWithMcp == null) {
            try {
                initMcp();
                botWithMcp = AiServices.builder(Bot.class)
                        .streamingChatModel(getBot())
                        .toolProvider(mcpToolProvider)
                        .build();
                mcpAvailable = true;
            } catch (Exception e) {
                System.err.println("MCP初始化失败: " + e.getMessage());
                mcpAvailable = false;
                return null;
            }
        }
        return botWithMcp;
    }

    public static Bot getBotWithoutMcp() {
        if (botWithoutMcp == null) {
            botWithoutMcp = AiServices.builder(Bot.class)
                    .streamingChatModel(getBot())
                    .build();
        }
        return botWithoutMcp;
    }

    private static void initMcp() {
        if (mcpToolProvider == null) {
            System.out.println("正在初始化MCP客户端连接...");
            
            McpTransport transport = new HttpMcpTransport.Builder()
                    .sseUrl("http://localhost:3001/sse")
                    .logRequests(true)
                    .logResponses(true)
                    .build();

            McpClient mcpClient = new DefaultMcpClient.Builder()
                    .transport(transport)
                    .build();

            mcpToolProvider = McpToolProvider.builder()
                    .mcpClients(List.of(mcpClient))
                    .build();
            
            System.out.println("MCP服务器连接成功");
        }
    }
}
