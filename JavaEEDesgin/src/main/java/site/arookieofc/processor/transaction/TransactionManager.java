package site.arookieofc.processor.transaction;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.transactional.Isolation;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.utils.DatabaseUtil;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

/**
 * 事务管理器
 */
@Slf4j
public class TransactionManager {
    
    // 使用ThreadLocal存储当前线程的事务状态栈
    private static final ThreadLocal<Stack<TransactionStatus>> transactionStack = 
        ThreadLocal.withInitial(Stack::new);
    
    /**
     * 开始事务
     */
    public static TransactionStatus begin(Propagation propagation, Isolation isolation) throws SQLException {
        Stack<TransactionStatus> stack = transactionStack.get();
        TransactionStatus currentStatus = stack.isEmpty() ? null : stack.peek();
        
        log.debug("开始事务, 传播行为: {}, 隔离级别: {}, 当前事务栈深度: {}", 
                propagation, isolation, stack.size());
        
        switch (propagation) {
            case REQUIRED:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    // 加入当前事务
                    log.debug("REQUIRED: 加入已存在的事务");
                    return currentStatus;
                } else {
                    // 创建新事务
                    log.debug("REQUIRED: 创建新事务");
                    return createNewTransaction(isolation);
                }
                
            case REQUIRES_NEW:
                // 总是创建新事务
                log.debug("REQUIRES_NEW: 创建新事务");
                return createNewTransaction(isolation);
                
            case SUPPORTS:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    log.debug("SUPPORTS: 加入已存在的事务");
                    return currentStatus;
                } else {
                    // 非事务方式运行
                    log.debug("SUPPORTS: 以非事务方式运行");
                    return createNonTransactionalStatus();
                }
                
            case NOT_SUPPORTED:
                // 非事务方式运行
                log.debug("NOT_SUPPORTED: 以非事务方式运行");
                return createNonTransactionalStatus();
                
            case NEVER:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    log.error("NEVER: 当前存在事务，但传播行为为NEVER");
                    throw new RuntimeException("当前存在事务，但传播行为为NEVER");
                }
                log.debug("NEVER: 以非事务方式运行");
                return createNonTransactionalStatus();
                
            case MANDATORY:
                if (currentStatus == null || currentStatus.isCompleted()) {
                    log.error("MANDATORY: 当前不存在事务，但传播行为为MANDATORY");
                    throw new RuntimeException("当前不存在事务，但传播行为为MANDATORY");
                }
                log.debug("MANDATORY: 加入已存在的事务");
                return currentStatus;
                
            default:
                log.debug("默认: 创建新事务");
                return createNewTransaction(isolation);
        }
    }
    
    /**
     * 创建新事务
     */
    private static TransactionStatus createNewTransaction(Isolation isolation) throws SQLException {
        Connection connection = DatabaseUtil.getConnection();

        // 设置隔离级别
        setIsolationLevel(connection, isolation);
        
        TransactionStatus status = new TransactionStatus(connection, true);
        transactionStack.get().push(status);
        log.debug("创建新事务, 隔离级别: {}, 当前事务栈深度: {}", isolation, transactionStack.get().size());
        return status;
    }
    
    /**
     * 创建非事务状态
     */
    private static TransactionStatus createNonTransactionalStatus() throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        
        TransactionStatus status = new TransactionStatus(connection, false);
        transactionStack.get().push(status);
        log.debug("创建非事务连接, 当前事务栈深度: {}", transactionStack.get().size());
        return status;
    }
    
    /**
     * 设置隔离级别
     */
    private static void setIsolationLevel(Connection connection, Isolation isolation) throws SQLException {
        int level = switch (isolation) {
            case READ_UNCOMMITTED -> Connection.TRANSACTION_READ_UNCOMMITTED;
            case REPEATABLE_READ -> Connection.TRANSACTION_REPEATABLE_READ;
            case SERIALIZABLE -> Connection.TRANSACTION_SERIALIZABLE;
            default -> Connection.TRANSACTION_READ_COMMITTED;
        };
        connection.setTransactionIsolation(level);
        log.debug("设置事务隔离级别: {}", isolation);
    }
    
    /**
     * 提交事务
     */
    public static void commit(TransactionStatus status) throws SQLException {
        if (status == null) {
            log.warn("尝试提交空事务状态");
            return;
        }
        
        if (status.isCompleted()) {
            log.warn("尝试提交已完成的事务");
            return;
        }
        
        Stack<TransactionStatus> stack = transactionStack.get();
        if (stack.isEmpty()) {
            log.error("事务栈为空，无法提交事务");
            throw new IllegalStateException("事务栈为空");
        }
        
        // 只有当前事务才能被提交
        if (stack.peek() != status) {
            log.warn("尝试提交非当前事务，忽略");
            return;
        }
        
        try {
            if (status.isTransactional()) {
                status.getConnection().commit();
                log.debug("事务已提交");
            }
        } finally {
            cleanupTransaction(status);
        }
    }
    
    /**
     * 回滚事务
     */
    public static void rollback(TransactionStatus status) throws SQLException {
        if (status == null) {
            log.warn("尝试回滚空事务状态");
            return;
        }
        
        if (status.isCompleted()) {
            log.warn("尝试回滚已完成的事务");
            return;
        }
        
        Stack<TransactionStatus> stack = transactionStack.get();
        if (stack.isEmpty()) {
            log.error("事务栈为空，无法回滚事务");
            throw new IllegalStateException("事务栈为空");
        }
        
        // 只有当前事务才能被回滚
        if (stack.peek() != status) {
            log.warn("尝试回滚非当前事务，忽略");
            return;
        }
        
        try {
            if (status.isTransactional()) {
                status.getConnection().rollback();
                log.debug("事务已回滚");
            }
        } finally {
            cleanupTransaction(status);
        }
    }
    
    /**
     * 清理事务资源
     */
    private static void cleanupTransaction(TransactionStatus status) {
        try {
            status.setCompleted();
            Stack<TransactionStatus> stack = transactionStack.get();
            stack.pop();
            log.debug("事务已清理，当前事务栈深度: {}", stack.size());
            
            if (!status.getConnection().isClosed()) {
                status.getConnection().close();
            }
        } catch (SQLException e) {
            log.error("关闭数据库连接失败", e);
        }
    }
    
    /**
     * 获取当前事务
     */
    public static TransactionStatus getCurrentTransaction() {
        Stack<TransactionStatus> stack = transactionStack.get();
        if (stack.isEmpty()) {
            return null;
        }
        TransactionStatus status = stack.peek();
        return status.isCompleted() ? null : status;
    }
}