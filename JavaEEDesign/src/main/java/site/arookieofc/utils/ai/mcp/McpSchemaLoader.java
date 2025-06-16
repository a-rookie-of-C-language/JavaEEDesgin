package site.arookieofc.utils.ai.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.annotation.ioc.Component;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
public class McpSchemaLoader {
    
    @Config(value = "mcp.schema.file", defaultValue = "mcp-schemas.json")
    private static String schemaFile;
    
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static Map<String, Object> schemas;
    private static boolean initialized = false;
    
    // 移除static块，改为延迟初始化
    
    @SuppressWarnings("unchecked")
    private static synchronized void loadSchemas() {
        if (initialized) {
            return;
        }
        
        try {
            // 如果配置注入失败，使用默认值
            String fileName = (schemaFile != null) ? schemaFile : "mcp-schemas.json";
            
            InputStream inputStream = McpSchemaLoader.class.getClassLoader().getResourceAsStream(fileName);
            if (inputStream != null) {
                Map<String, Object> config = jsonMapper.readValue(inputStream, Map.class);
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
        // 延迟加载
        if (!initialized) {
            loadSchemas();
        }
        
        try {
            Object schema = schemas.get(schemaName);
            if (schema == null) {
                throw new IllegalArgumentException("未找到schema: " + schemaName);
            }
            return jsonMapper.writeValueAsString(schema);
        } catch (Exception e) {
            log.error("获取schema失败: {}", schemaName, e);
            throw new RuntimeException("获取schema失败: " + schemaName, e);
        }
    }
}