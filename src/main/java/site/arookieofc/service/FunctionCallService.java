package site.arookieofc.service;

import java.util.Map;

public interface FunctionCallService {
    
    /**
     * 执行函数调用
     */
    Object executeFunction(String functionName, Map<String, Object> parameters);
    
    /**
     * 获取可用的函数列表
     */
    Map<String, Object> getAvailableFunctions();
    
    /**
     * 注册新的函数
     */
    void registerFunction(String functionName, Object functionHandler);
}