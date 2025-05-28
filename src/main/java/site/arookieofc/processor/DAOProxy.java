package site.arookieofc.processor;

import site.arookieofc.annotation.SQL;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class DAOProxy implements InvocationHandler {
    
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> daoInterface) {
        return (T) Proxy.newProxyInstance(
            daoInterface.getClassLoader(),
            new Class[]{daoInterface},
            new DAOProxy()
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(SQL.class)) {
            return SQLExecutor.executeSQL(method, args, method.getReturnType());
        }
        
        // 如果没有@SQL注解，抛出异常
        throw new UnsupportedOperationException("Method " + method.getName() + " must be annotated with @SQL");
    }
}