package site.arookieofc.processor.validation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.validation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;

/**
 * 验证拦截器
 * 用于自动处理方法参数上的验证注解
 */
@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationInterceptor implements InvocationHandler {
    
    private final Object target;

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target) {
        // 获取目标类实现的所有接口，包括父类实现的接口
        Class<?>[] interfaces = getAllInterfaces(target.getClass());

        log.debug("为目标对象创建验证代理: {}, 实现接口数量: {}",
                target.getClass().getName(),
                interfaces.length);

        return (T) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new ValidationInterceptor(target)
        );
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (needsValidation(method)) {
            intercept(method, args);
        }
        return method.invoke(target, args);
    }


    private boolean needsValidation(Method method) {
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            // 检查参数本身的注解
            if (hasValidationAnnotation(parameter)) {
                return true;
            }
            
            // 检查参数类型的字段是否有验证注解
            Class<?> paramType = parameter.getType();
            if (notPrimitiveOrWrapper(paramType)) {
                if (hasFieldValidationAnnotations(paramType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasFieldValidationAnnotations(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Need.class) ||
                field.isAnnotationPresent(NotNull.class) ||
                field.isAnnotationPresent(NotEmpty.class) ||
                field.isAnnotationPresent(Range.class) ||
                field.isAnnotationPresent(Size.class) ||
                field.isAnnotationPresent(Exists.class)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasValidationAnnotation(Parameter parameter) {
        return parameter.isAnnotationPresent(Need.class) ||
               parameter.isAnnotationPresent(NotNull.class) ||
               parameter.isAnnotationPresent(NotEmpty.class) ||
               parameter.isAnnotationPresent(Range.class) ||
               parameter.isAnnotationPresent(Size.class) ||
               parameter.isAnnotationPresent(Exists.class);
    }

    public static Annotation[] getValidationAnnotations(Parameter parameter) {
        return parameter.getAnnotations();
    }
    

    public static void validateParameter(Parameter parameter, Object value, String paramName) {
        log.debug("验证参数 {} 的值: {}", paramName, value);
        
        // 检查@Need注解
        if (parameter.isAnnotationPresent(Need.class)) {
            Need annotation = parameter.getAnnotation(Need.class);
            ValidationProcessor.validateNeed(value, paramName, annotation.message());
        }
        
        // 检查@NotNull注解
        if (parameter.isAnnotationPresent(NotNull.class)) {
            NotNull annotation = parameter.getAnnotation(NotNull.class);
            ValidationProcessor.validateNotNull(value, paramName, annotation.message());
        }
        
        // 检查@NotEmpty注解
        if (parameter.isAnnotationPresent(NotEmpty.class)) {
            NotEmpty annotation = parameter.getAnnotation(NotEmpty.class);
            ValidationProcessor.validateNotEmpty(value, paramName, annotation.message());
        }
        
        // 检查@Range注解
        if (parameter.isAnnotationPresent(Range.class)) {
            Range annotation = parameter.getAnnotation(Range.class);
            ValidationProcessor.validateRange(value, paramName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Size注解
        if (parameter.isAnnotationPresent(Size.class)) {
            Size annotation = parameter.getAnnotation(Size.class);
            ValidationProcessor.validateSize(value, paramName, annotation.min(), annotation.max(), annotation.message());
        }
        
        // 检查@Exists注解
        if (parameter.isAnnotationPresent(Exists.class)) {
            Exists annotation = parameter.getAnnotation(Exists.class);
            ValidationProcessor.validateExists(value, paramName, annotation.message());
        }
        
        log.debug("参数 {} 验证通过", paramName);
    }
    


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


    public static void intercept(Method method, Object[] args) {
        log.debug("开始验证方法 {} 的参数", method.getName());
        
        try {
            // 验证方法参数上的注解
            ValidationProcessor.validateMethodParameters(method, args);
            
            // 验证参数对象内部字段的注解
            if (args != null) {
                for (Object arg : args) {
                    if (arg != null && notPrimitiveOrWrapper(arg.getClass())) {
                        ValidationProcessor.validateObject(arg);
                    }
                }
            }
            
            log.debug("方法 {} 参数验证通过", method.getName());
        } catch (IllegalArgumentException e) {
            log.warn("方法 {} 参数验证失败: {}", method.getName(), e.getMessage());
            throw e;
        }
    }

    private static boolean notPrimitiveOrWrapper(Class<?> clazz) {
        return !clazz.isPrimitive() &&
                clazz != String.class &&
                clazz != Integer.class &&
                clazz != Long.class &&
                clazz != Double.class &&
                clazz != Float.class &&
                clazz != Boolean.class &&
                clazz != Character.class &&
                clazz != Byte.class &&
                clazz != Short.class;
    }
}