package site.arookieofc.server;

import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import site.arookieofc.annotation.config.Config;
import site.arookieofc.processor.config.ConfigProcessor;
import site.arookieofc.utils.ai.mcp.BusinessToolManager;
import site.arookieofc.utils.JsonUtils;

@Slf4j
public class McpServer {

    static {
        ConfigProcessor.injectStaticFields(McpServer.class);
    }

    @Config(value = "mcp.server.port" ,defaultValue = "3001")
    private static int mcpPort;
    
    @Config(value = "mcp.server.name",defaultValue = "business-mcp-server")
    private static String serverName;
    
    @Config(value = "mcp.server.version",defaultValue ="1.0.0" )
    private static String serverVersion;
    
    @Config(value = "mcp.server.sse-endpoint",defaultValue =  "/sse")
    private static String sseEndpoint;
    
    @Config(value = "mcp.server.message-endpoint",defaultValue = "/message")
    private static String messageEndpoint;

    private static HttpServletSseServerTransportProvider transport;
    private static McpSyncServer syncServer;

    /**
     * 初始化并启动MCP服务器
     */
    public static void initializeAndStart() {
        try {
            log.info("正在初始化MCP服务器...");
            initializeServer();
            startServer();
            log.info("MCP服务器初始化完成");
        } catch (Exception e) {
            log.error("MCP服务器初始化失败", e);
            throw new RuntimeException("MCP服务器初始化失败", e);
        }
    }

    /**
     * 初始化MCP服务器配置
     */
    private static void initializeServer() {
        log.info("MCP服务器配置: 端口={}, 名称={}, 版本={}", mcpPort, serverName, serverVersion);
        transport = HttpServletSseServerTransportProvider.builder()
                .messageEndpoint(messageEndpoint)
                .objectMapper(JsonUtils.getObjectMapper())
                .build();
        // 创建同步服务器
        syncServer = io.modelcontextprotocol.server.McpServer.sync(transport)
                .serverInfo(serverName, serverVersion)
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .resources(true, true)
                        .tools(true)
                        .prompts(true)
                        .logging()
                        .build())
                .build();
        // 注册业务工具
        registerBusinessTools();
    }

    /**
     * 注册所有业务工具
     */
    private static void registerBusinessTools() {
        log.info("注册业务工具...");
        
        // 获取BusinessToolManager单例实例
        BusinessToolManager toolManager = BusinessToolManager.getInstance();
        
        // 学生管理工具
        syncServer.addTool(toolManager.createStudentQueryTool());
        syncServer.addTool(toolManager.createStudentAddTool());
        syncServer.addTool(toolManager.createStudentUpdateTool());
        syncServer.addTool(toolManager.createStudentDeleteTool());

        // 教师管理工具
        syncServer.addTool(toolManager.createTeacherQueryTool());
        syncServer.addTool(toolManager.createTeacherAddTool());
        syncServer.addTool(toolManager.createTeacherUpdateTool());
        syncServer.addTool(toolManager.createTeacherDeleteTool());

        // 班级管理工具
        syncServer.addTool(toolManager.createClazzQueryTool());
        syncServer.addTool(toolManager.createClazzAddTool());
        syncServer.addTool(toolManager.createClazzUpdateTool());
        syncServer.addTool(toolManager.createClazzDeleteTool());
        
        log.info("业务工具注册完成");
    }

    /**
     * 启动服务器
     */
    private static void startServer() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(mcpPort);
        String baseDir = System.getProperty("java.io.tmpdir");
        tomcat.setBaseDir(baseDir);
        
        // 设置UTF-8编码
        tomcat.getConnector().setURIEncoding("UTF-8");
        
        Context context = tomcat.addContext("", baseDir);
        Tomcat.addServlet(context, "transportProvider", transport);
        context.addServletMappingDecoded(sseEndpoint, "transportProvider");
        context.addServletMappingDecoded(messageEndpoint, "transportProvider");
        
        tomcat.start();
        tomcat.getConnector();

        // 发送启动通知
        syncServer.loggingNotification(McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.INFO)
                .logger("mcp-server")
                .data(String.format("MCP server %s v%s started on port %d", serverName, serverVersion, mcpPort))
                .build());

        log.info("MCP Server started on port {}", mcpPort);
        log.info("SSE endpoint: http://localhost:{}{}", mcpPort, sseEndpoint);
        log.info("Message endpoint: http://localhost:{}{}", mcpPort, messageEndpoint);
    }
}