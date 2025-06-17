package site.arookieofc.annotation.transactional;

/**
 * 事务传播行为
 */
public enum Propagation {
    REQUIRED,

    REQUIRES_NEW,

    SUPPORTS,

    NOT_SUPPORTED,

    NEVER,

    MANDATORY
}