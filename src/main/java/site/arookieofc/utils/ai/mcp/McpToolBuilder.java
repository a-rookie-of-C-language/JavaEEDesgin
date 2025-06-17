package site.arookieofc.utils.ai.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Slf4j
public class McpToolBuilder {

    public static McpServerFeatures.@NotNull SyncToolSpecification createToolSpecification(
            String toolName,
            String description,
            String schemaName,
            BiFunction<McpSyncServerExchange, Map<String, Object>, String> handler,
            String errorPrefix) {

        String schema = McpSchemaLoader.getSchemaJson(schemaName);

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(toolName, description, schema),
                (exchange, arguments) -> {
                    try {
                        String result = handler.apply(exchange, arguments);
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(result)),
                                false
                        );
                    } catch (Exception e) {
                        log.error("执行工具{}失败", toolName, e);
                        String errorMessage = e.getMessage() != null ? e.getMessage() : "未知错误";
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(errorPrefix + ": " + errorMessage)),
                                true
                        );
                    }
                }
        );
    }

    public static McpServerFeatures.@NotNull SyncToolSpecification createDeleteToolSpecification(
            String toolName,
            String description,
            java.util.function.Consumer<String> deleteAction,
            String successPrefix,
            String errorPrefix) {

        String schema = McpSchemaLoader.getSchemaJson("id_only");

        return new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool(toolName, description, schema),
                (exchange, arguments) -> {
                    try {
                        String id = (String) arguments.get("id");
                        deleteAction.accept(id);
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(successPrefix + ": " + id)),
                                false
                        );
                    } catch (Exception e) {
                        log.error("执行删除工具{}失败", toolName, e);
                        return new McpSchema.CallToolResult(
                                List.of(new McpSchema.TextContent(errorPrefix + ": " + e.getMessage())),
                                true
                        );
                    }
                }
        );
    }
}