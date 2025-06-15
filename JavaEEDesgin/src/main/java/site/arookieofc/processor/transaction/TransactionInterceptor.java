package site.arookieofc.processor.transaction;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.ioc.Bean;
import site.arookieofc.annotation.transactional.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Arrays;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransactionInterceptor implements InvocationHandler {
    
    private final Object target;

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        // 获取目标类实现的所有接口，包括父类实现的接口
        Class<?>[] interfaces = getAllInterfaces(target.getClass());

        log.debug("为目标对象创建事务代理: {}, 实现接口数量: {}",
                target.getClass().getName(),
                interfaces.length);

        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new TransactionInterceptor(target)
        );
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
        log.debug("开始执行事务方法: {}.{}", target.getClass().getSimpleName(), method.getName());
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
        long startTime = System.currentTimeMillis();
        
        try {
            // 开始事务
            status = TransactionManager.begin(transactional.propagation(), transactional.isolation());
            log.debug("事务已开始: {}.{}, 传播行为: {}, 隔离级别: {}", 
                    target.getClass().getSimpleName(), 
                    method.getName(), 
                    transactional.propagation(), 
                    transactional.isolation());
            
            // 执行目标方法
            Object result = method.invoke(target, args);
            
            // 提交事务
            TransactionManager.commit(status);
            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("事务已提交: {}.{}, 耗时: {}ms", 
                    target.getClass().getSimpleName(), 
                    method.getName(), 
                    executionTime);
            return result;
            
        } catch (Throwable ex) {
            // 检查是否应该回滚事务
            if (status != null) {
                // 获取实际异常
                Throwable actualException = ex;
                if (ex instanceof java.lang.reflect.InvocationTargetException) {
                    Throwable targetEx = ((java.lang.reflect.InvocationTargetException) ex).getTargetException();
                    if (targetEx != null) {
                        actualException = targetEx;
                    }
                }
                
                // 检查是否应该回滚
                if (shouldRollback(transactional, actualException)) {
                    log.error("执行事务回滚: {}.{}, 异常类型: {}, 异常信息: {}", 
                            target.getClass().getSimpleName(), 
                            method.getName(), 
                            actualException.getClass().getName(), 
                            actualException.getMessage());
                    TransactionManager.rollback(status);
                } else {
                    log.debug("不回滚事务: {}.{}, 异常类型: {} 不在rollbackFor列表中", 
                            target.getClass().getSimpleName(), 
                            method.getName(), 
                            actualException.getClass().getName());
                    try {
                        TransactionManager.commit(status);
                    } catch (SQLException e) {
                        log.error("提交事务失败", e);
                    }
                }
            }
            
            // 改进的异常处理逻辑
            Throwable actualException = ex;
            
            // 处理InvocationTargetException
            if (ex instanceof java.lang.reflect.InvocationTargetException) {
                Throwable targetEx = ((java.lang.reflect.InvocationTargetException) ex).getTargetException();
                if (targetEx != null) {
                    actualException = targetEx;
                    log.error("事务方法执行异常: {}.{}, 目标异常: {}, 异常信息: {}", 
                            target.getClass().getSimpleName(), 
                            method.getName(), 
                            targetEx.getClass().getName(), 
                            targetEx.getMessage(), 
                            targetEx);
                }
            } else {
                log.error("事务方法执行异常: {}.{}, 异常信息: {}", 
                        target.getClass().getSimpleName(), 
                        method.getName(), 
                        ex.getMessage(), 
                        ex);
            }
            
            throw actualException;
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
     * 获取类实现的所有接口，包括父类实现的接口
     */
    private static Class<?>[] getAllInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return new Class[0];
        }
        java.util.Set<Class<?>> interfacesSet = new java.util.LinkedHashSet<>(Arrays.asList(clazz.getInterfaces()));
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            interfacesSet.addAll(Arrays.asList(getAllInterfaces(superclass)));
        }
        return interfacesSet.toArray(new Class<?>[0]);
    }
}