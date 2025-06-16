
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolProvider;
import site.arookieofc.service.impl.Bot;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SseText {
    public static void main(String[] args) throws Exception {

        StreamingChatModel model = OllamaStreamingChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("qwen3:14b")
                .logRequests(true)
                .logResponses(true)
                .build();

        McpTransport transport = new HttpMcpTransport.Builder()
                .sseUrl("http://localhost:3001/sse")
                .logRequests(true)
                .logResponses(true)
                .build();

        McpClient mcpClient = new DefaultMcpClient.Builder()
                .transport(transport)
                .build();

        ToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(List.of(mcpClient))
                .build();

        Bot bot = AiServices.builder(Bot.class)
                .streamingChatModel(model)
                .toolProvider(toolProvider)
                .build();

        try {
            System.out.println("开始流式输出：");
            CountDownLatch latch = new CountDownLatch(1);
            
            TokenStream tokenStream = bot.chat("查询老师列表");
            tokenStream
                    .onPartialResponse(token -> {
                        System.out.print(token); // 实时输出每个token
                        System.out.flush(); // 强制刷新输出缓冲区
                    })
                    .onCompleteResponse(completeResponse -> {
                        System.out.println("\n\n=== 完整响应 ===");
                        System.out.println(completeResponse);
                        latch.countDown(); // 标记完成
                    })
                    .onError(error -> {
                        System.err.println("\n错误: " + error.getMessage());
                        error.printStackTrace();
                        latch.countDown(); // 即使出错也要标记完成
                    })
                    .start();
            
            // 等待流式输出完成
            latch.await();
            System.out.println("\n流式输出完成！");
        } finally {
            mcpClient.close();
        }
    }
}