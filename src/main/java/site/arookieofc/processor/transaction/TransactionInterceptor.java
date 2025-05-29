package site.arookieofc.processor.transaction;

import site.arookieofc.annotation.transactional.Transactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class TransactionInterceptor implements InvocationHandler {
    
    private final Object target;
    
    public TransactionInterceptor(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 检查方法或类是否有@Transactional注解
        Transactional transactional = getTransactionalAnnotation(method);
        
        if (transactional == null) {
            // 没有事务注解，直接执行
            return method.invoke(target, args);
        }
        
        // 有事务注解，在事务中执行
        return executeInTransaction(transactional, method, args);
    }
    
    /**
     * 获取事务注解
     */
    private Transactional getTransactionalAnnotation(Method method) {
        // 先检查方法级别的注解
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional != null) {
            return transactional;
        }
        
        // 再检查类级别的注解
        return target.getClass().getAnnotation(Transactional.class);
    }
    
    /**
     * 在事务中执行方法
     */
    private Object executeInTransaction(Transactional transactional, Method method, Object[] args) throws Throwable {
        TransactionStatus status = null;
        try {
            // 开始事务
            status = TransactionManager.begin(transactional.propagation(), transactional.isolation());
            
            // 执行目标方法
            Object result = method.invoke(target, args);
            
            // 提交事务
            TransactionManager.commit(status);
            return result;
            
        } catch (Throwable ex) {
            // 检查是否需要回滚
            if (shouldRollback(transactional, ex)) {
                if (status != null) {
                    TransactionManager.rollback(status);
                }
            } else {
                // 不需要回滚，提交事务
                if (status != null) {
                    TransactionManager.commit(status);
                }
            }
            
            // 提取InvocationTargetException中的目标异常
            if (ex instanceof java.lang.reflect.InvocationTargetException) {
                throw ((java.lang.reflect.InvocationTargetException) ex).getTargetException();
            }
            throw ex;
        }
    }
    
    /**
     * 判断是否需要回滚
     */
    private boolean shouldRollback(Transactional transactional, Throwable ex) {
        Class<? extends Throwable>[] rollbackFor = transactional.rollbackFor();
        
        for (Class<? extends Throwable> rollbackClass : rollbackFor) {
            if (rollbackClass.isAssignableFrom(ex.getClass())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            target.getClass().getInterfaces(),
            new TransactionInterceptor(target)
        );
    }
}