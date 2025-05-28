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
        
        // 从缓存中获取DAO实例
        T dao = (T) daoCache.get(daoInterface);
        if (dao != null) {
            return dao;
        }
        
        // 使用动态代理创建DAO实现
        dao = (T) Proxy.newProxyInstance(
            daoInterface.getClassLoader(),
            new Class<?>[]{daoInterface},
            (proxy, method, args) -> {
                try {
                    // 使用SQLExecutor执行SQL方法
                    return SQLExecutor.executeSQL(method, args, method.getReturnType());
                } catch (Exception e) {
                    throw new RuntimeException("执行DAO方法失败: " + method.getName(), e);
                }
            }
        );
        
        // 缓存DAO实例
        daoCache.put(daoInterface, dao);
        return dao;
    }
    
    /**
     * 清除DAO缓存
     */
    public static void clearCache() {
        daoCache.clear();
    }
    
    /**
     * 获取缓存大小
     */
    public static int getCacheSize() {
        return daoCache.size();
    }
}