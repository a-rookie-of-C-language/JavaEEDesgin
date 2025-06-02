package site.arookieofc.processor.sql;

import site.arookieofc.annotation.sql.SQL;
import site.arookieofc.processor.transaction.TransactionManager;
import site.arookieofc.processor.transaction.TransactionStatus;
import site.arookieofc.utils.DatabaseUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLExecutor {
    
    @SuppressWarnings("unchecked")
    public static <T> T executeSQL(Method method, Object[] args, Class<T> returnType) {
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        if (sqlAnnotation == null) {
            throw new RuntimeException("Method must be annotated with @SQL");
        }
        
        String sql = sqlAnnotation.value();
        String type = sqlAnnotation.type().toUpperCase();
        
        // 首先尝试获取当前事务的连接
        Connection conn = null;
        boolean closeConnection = true;
        boolean isTransactional = false;
        
        try {
            TransactionStatus currentStatus = TransactionManager.getCurrentTransaction();
            if (currentStatus != null && !currentStatus.isCompleted()) {
                conn = currentStatus.getConnection();
                closeConnection = false; // 不关闭事务连接
                isTransactional = true;  // 标记为事务环境
            } else {
                conn = DatabaseUtil.getConnection();
            }
            
            try {
                Object result;
                if ("SELECT".equals(type)) {
                    result = executeQuery(conn, sql, args, returnType, method);
                } else {
                    result = executeUpdate(conn, sql, args, returnType);
                }
                
                // 如果不是在事务环境中，手动提交
                if (!isTransactional) {
                    conn.commit();
                }
                
                return (T) result;
            } finally {
                if (closeConnection && conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // 忽略关闭连接时的异常
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL execution failed", e);
        }
    }
    
    private static Object executeQuery(Connection conn, String sql, Object[] args, Class<?> returnType, Method method) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, args);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (returnType == Optional.class) {
                    Class<?> entityType = getGenericType(method);
                    if (entityType.equals(List.class)) {
                        Class<?> listEntityType = getListGenericType(method);
                        List<Object> results = mapResultSetToList(rs, listEntityType);
                        return Optional.ofNullable(results.isEmpty() ? null : results);
                    } else {
                        if (rs.next()) {
                            Object entity = mapResultSetToEntity(rs, entityType);
                            return Optional.of(entity);
                        } else {
                            return Optional.empty();
                        }
                    }
                } else if (returnType == List.class) {
                    Class<?> entityType = getListGenericType(method);
                    return mapResultSetToList(rs, entityType);
                } else {
                    if (rs.next()) {
                        return mapResultSetToEntity(rs, returnType);
                    }
                    return null;
                }
            }
        }
    }
    
    private static Object executeUpdate(Connection conn, String sql, Object[] args, Class<?> returnType) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            setParameters(stmt, args);
            int affectedRows = stmt.executeUpdate();
            
            if (returnType == int.class || returnType == Integer.class) {
                return affectedRows;
            } else if (returnType == boolean.class || returnType == Boolean.class) {
                return affectedRows > 0;
            } else if (returnType == void.class || returnType == Void.class) {
                return null;
            }
            
            return affectedRows;
        }
    }
    
    private static void setParameters(PreparedStatement stmt, Object[] args) throws SQLException {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }
        }
    }
    
    private static Class<?> getListGenericType(Method method) {
        try {
            java.lang.reflect.ParameterizedType parameterizedType = 
                (java.lang.reflect.ParameterizedType) method.getGenericReturnType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Cannot determine list generic type", e);
        }
    }
    
    private static Object mapResultSetToEntity(ResultSet rs, Class<?> entityType) {
        try {
            Object entity = entityType.getDeclaredConstructor().newInstance();
            Field[] fields = entityType.getDeclaredFields();
            
            System.out.println("Mapping entity: " + entityType.getSimpleName());
            
            for (Field field : fields) {
                String columnName = field.getName();
                String setterName = "set" + Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1);
                
                try {
                    Method setter = entityType.getMethod(setterName, field.getType());
                    Object value = null;
                    
                    // 优先尝试下划线格式
                    try {
                        String underscoreColumnName = camelToUnderscore(columnName);
                        value = rs.getObject(underscoreColumnName);
                    } catch (SQLException e1) {
                        // 如果下划线格式失败，尝试驼峰格式
                        try {
                            value = rs.getObject(columnName);
                        } catch (SQLException e2) {
                            System.out.println("Failed to map field: " + columnName);
                            continue;
                        }
                    }
                    
                    if (value != null) {
                        // 类型转换逻辑保持不变
                        if (field.getType() == String.class && !(value instanceof String)) {
                            value = value.toString();
                        } else if (field.getType() == Integer.class && value instanceof Number) {
                            value = ((Number) value).intValue();
                        }
                        setter.invoke(entity, value);
                    }
                } catch (Exception e) {
                    System.out.println("Error setting field " + columnName + ": " + e.getMessage());
                }
            }
            
            System.out.println("Final entity: " + entity);
            return entity;
        } catch (Exception e) {
            System.out.println("Failed to create entity: " + e.getMessage());
            throw new RuntimeException("Failed to map ResultSet to entity", e);
        }
    }

    private static String camelToUnderscore(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    private static List<Object> mapResultSetToList(ResultSet rs, Class<?> entityType) throws SQLException {
        List<Object> results = new ArrayList<>();
        
        // 检查是否为基本类型或包装类型
        if (isPrimitiveOrWrapper(entityType)) {
            while (rs.next()) {
                // 对于基本类型，直接从第一列获取值
                Object value = rs.getObject(1);
                if (value != null) {
                    // 进行必要的类型转换
                    if (entityType == String.class) {
                        results.add(value.toString());
                    } else if (entityType == Integer.class && value instanceof Number) {
                        results.add(((Number) value).intValue());
                    } else {
                        results.add(value);
                    }
                }
            }
        } else {
            // 对于复杂对象，使用原有的映射逻辑
            while (rs.next()) {
                results.add(mapResultSetToEntity(rs, entityType));
            }
        }
        
        return results;
    }
    
    // 添加辅助方法来检查是否为基本类型
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Boolean.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class;
    }
    
    private static Class<?> getGenericType(Method method) {
        try {
            java.lang.reflect.ParameterizedType parameterizedType = 
                (java.lang.reflect.ParameterizedType) method.getGenericReturnType();
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Cannot determine generic type", e);
        }
    }
}