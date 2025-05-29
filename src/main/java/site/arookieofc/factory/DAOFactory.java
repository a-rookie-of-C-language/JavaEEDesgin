package site.arookieofc.factory;

import site.arookieofc.processor.SQLExecutor;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class DAOFactory {
    
    private static final Map<Class<?>, Object> daoCache = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public static <T> T getDAO(Class<T> daoInterface) {
        if (daoInterface == null) {
            throw new IllegalArgumentException("DAO接口不能为空");
        }
        
        if (!daoInterface.isInterface()) {
            throw new IllegalArgumentException("必须是接口类型: " + daoInterface.getName());
        }

        T dao = (T) daoCache.get(daoInterface);
        if (dao != null) {
            return dao;
        }

        dao = (T) Proxy.newProxyInstance(
            daoInterface.getClassLoader(),
            new Class<?>[]{daoInterface},
            (proxy, method, args) -> {
                try {
                    return SQLExecutor.executeSQL(method, args, method.getReturnType());
                } catch (Exception e) {
                    throw new RuntimeException("执行DAO方法失败: " + method.getName(), e);
                }
            }
        );

        daoCache.put(daoInterface, dao);
        return dao;
    }
}