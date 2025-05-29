package site.arookieofc.annotation.transactional;

/**
 * 事务传播行为
 */
public enum Propagation {
    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
     */
    REQUIRED,
    
    /**
     * 创建一个新的事务，如果当前存在事务，则把当前事务挂起
     */
    REQUIRES_NEW,
    
    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行
     */
    SUPPORTS,
    
    /**
     * 以非事务方式运行，如果当前存在事务，则把当前事务挂起
     */
    NOT_SUPPORTED,
    
    /**
     * 以非事务方式运行，如果当前存在事务，则抛出异常
     */
    NEVER,
    
    /**
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常
     */
    MANDATORY
}