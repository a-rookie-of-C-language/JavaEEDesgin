package site.arookieofc.service;

import java.util.Map;

public interface FunctionCallService {

    Object executeFunction(String functionName, Map<String, Object> parameters);

    Map<String, Object> getAvailableFunctions();

    void registerFunction(String functionName, Object functionHandler);
}