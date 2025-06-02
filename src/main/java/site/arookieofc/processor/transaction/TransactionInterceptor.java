package site.arookieofc.processor.transaction;

import site.arookieofc.annotation.transactional.Transactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

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
            // 改进：始终回滚RuntimeException，不再检查shouldRollback
            if (status != null) {
                System.out.println("执行事务回滚: " + ex.getClass().getName());
                TransactionManager.rollback(status);
            }
            
            // 改进的异常处理逻辑
            Throwable actualException = ex;
            
            // 处理InvocationTargetException
            if (ex instanceof java.lang.reflect.InvocationTargetException) {
                Throwable targetEx = ((java.lang.reflect.InvocationTargetException) ex).getTargetException();
                if (targetEx != null) {
                    actualException = targetEx;
                }
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
     * 创建代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        // 获取目标类实现的所有接口，包括父类实现的接口
        Class<?>[] interfaces = getAllInterfaces(target.getClass());
        
        return (T) Proxy.newProxyInstance(
            target.getClass().getClassLoader(),
            interfaces,
            new TransactionInterceptor(target)
        );
    }

    /**
     * 获取类实现的所有接口，包括父类实现的接口
     */
    private static Class<?>[] getAllInterfaces(Class<?> clazz) {
        if (clazz == null) {
            return new Class[0];
        }
        
        // 使用Set避免重复接口

        // 添加当前类实现的接口
        java.util.Set<Class<?>> interfacesSet = new java.util.LinkedHashSet<>(Arrays.asList(clazz.getInterfaces()));
        
        // 递归添加父类实现的接口
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && superclass != Object.class) {
            interfacesSet.addAll(Arrays.asList(getAllInterfaces(superclass)));
        }
        
        return interfacesSet.toArray(new Class<?>[0]);
    }
}