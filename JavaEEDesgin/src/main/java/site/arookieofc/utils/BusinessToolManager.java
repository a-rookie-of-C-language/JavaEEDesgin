package site.arookieofc.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.jetbrains.annotations.NotNull;

public class BusinessToolManager {
    public static void main(String[] args) {
        StdioServerTransportProvider transport = new StdioServerTransportProvider(new ObjectMapper());
        // 使用自定义配置创建服务器
        McpSyncServer syncServer = McpServer.sync(transport)
                .serverInfo("my-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(true,true)     // 启用资源支持
                        .tools(true)         // 启用工具支持
                        .prompts(true)       // 启用提示支持
                        .logging()           // 启用日志支持
                        .build())
                .build();
        var syncToolSpecification = getSyncToolSpecification();
        syncServer.addTool(syncToolSpecification);

    }

    private static McpServerFeatures.@NotNull SyncToolSpecification getSyncToolSpecification() {
        var schema = """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {
                "operation" : {
                  "type" : "string"
                },
                "a" : {
                  "type" : "number"
                },
                "b" : {
                  "type" : "number"
                }
              }
            }
            """;
        // Tool implementation
        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("calculator", "Basic calculator", schema),
                (exchange, arguments) -> {
                    // Tool implementation
                    String result = add((int)arguments.get("a"),(int)arguments.get("b"),(String) arguments.get("operation"));
                    return new McpSchema.CallToolResult(result, false);
                }
        );
    }
    private static String add(int a,int b,String operation){
        switch (operation){
            case "+" -> {
                return  String.valueOf(a+b);
            }
            case "-" -> {
                return  String.valueOf(a-b);
            }
            case "*" -> {
                return  String.valueOf(a*b);
            }
            case "/" -> {
                return  String.valueOf(a/b);
            }
            default -> {
                return  "";
            }
        }
    }
}