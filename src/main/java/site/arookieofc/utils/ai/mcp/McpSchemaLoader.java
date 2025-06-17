package site.arookieofc.utils.ai.mcp;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.ioc.Component;
import site.arookieofc.utils.JsonUtils;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
public class McpSchemaLoader {
    
    @Config(value = "mcp.schema.file", defaultValue = "mcp-schemas.json")
    private static String schemaFile;

    private static Map<String, Object> schemas;
    private static boolean initialized = false;
    
    @SuppressWarnings("unchecked")
    private static synchronized void loadSchemas() {
        if (initialized) {
            return;
        }
        try {
            String fileName = (schemaFile != null) ? schemaFile : "mcp-schemas.json";
            InputStream inputStream = McpSchemaLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                Map<String, Object> config = JsonUtils.getObjectMapper().readValue(inputStream, Map.class);
                schemas = (Map<String, Object>) config.get("schemas");
                log.info("成功加载MCP Schema配置，共{}个schema，文件: {}", schemas.size(), fileName);
                initialized = true;
            } else {
                log.error("未找到schema配置文件: {}", fileName);
                throw new RuntimeException("Schema配置文件未找到: " + fileName);
            }
        } catch (Exception e) {
            log.error("加载schema配置失败", e);
            throw new RuntimeException("加载schema配置失败", e);
        }
    }
    
    public static String getSchemaJson(String schemaName) {
        if (!initialized) {
            loadSchemas();
        }
        
        try {
            Object schema = schemas.get(schemaName);
            if (schema == null) {
                throw new IllegalArgumentException("未找到schema: " + schemaName);
            }
            return JsonUtils.getObjectMapper().writeValueAsString(schema);
        } catch (Exception e) {
            log.error("获取schema失败: {}", schemaName, e);
            throw new RuntimeException("获取schema失败: " + schemaName, e);
        }
    }
}