package site.arookieofc.processor;

import site.arookieofc.annotation.sql.SQL;
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
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            if ("SELECT".equals(type)) {
                return (T) executeQuery(conn, sql, args, returnType, method);
            } else {
                return (T) executeUpdate(conn, sql, args, returnType);
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
    
    private static Object mapResultSetToEntity(ResultSet rs, Class<?> entityType) throws SQLException {
        try {
            Object entity = entityType.getDeclaredConstructor().newInstance();
            Field[] fields = entityType.getDeclaredFields();
            
            for (Field field : fields) {
                field.setAccessible(true);
                String columnName = field.getName();
                
                try {
                    Object value = rs.getObject(columnName);
                    if (value != null) {
                        field.set(entity, value);
                    }
                } catch (SQLException e) {
                    // 忽略不存在的列
                }
            }
            
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to entity", e);
        }
    }
    
    private static List<Object> mapResultSetToList(ResultSet rs, Class<?> entityType) throws SQLException {
        List<Object> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapResultSetToEntity(rs, entityType));
        }
        return results;
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
    
    private static Class<?> getListGenericType(Method method) {
        try {
            java.lang.reflect.ParameterizedType optionalType = 
                (java.lang.reflect.ParameterizedType) method.getGenericReturnType();
            java.lang.reflect.ParameterizedType listType = 
                (java.lang.reflect.ParameterizedType) optionalType.getActualTypeArguments()[0];
            return (Class<?>) listType.getActualTypeArguments()[0];
        } catch (Exception e) {
            throw new RuntimeException("Cannot determine list generic type", e);
        }
    }
}