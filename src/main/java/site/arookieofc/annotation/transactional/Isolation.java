package site.arookieofc.annotation.transactional;

/**
 * 事务隔离级别
 */
public enum Isolation {
    DEFAULT,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}