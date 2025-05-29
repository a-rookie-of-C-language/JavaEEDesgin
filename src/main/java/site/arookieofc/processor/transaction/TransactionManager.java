package site.arookieofc.processor.transaction;

import site.arookieofc.annotation.transactional.Isolation;
import site.arookieofc.annotation.transactional.Propagation;
import site.arookieofc.utils.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Stack;

/**
 * 事务管理器
 */
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
        
        switch (propagation) {
            case REQUIRED:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    // 加入当前事务
                    return currentStatus;
                } else {
                    // 创建新事务
                    return createNewTransaction(isolation);
                }
                
            case REQUIRES_NEW:
                // 总是创建新事务
                return createNewTransaction(isolation);
                
            case SUPPORTS:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    return currentStatus;
                } else {
                    // 非事务方式运行
                    return createNonTransactionalStatus();
                }
                
            case NOT_SUPPORTED:
                // 非事务方式运行
                return createNonTransactionalStatus();
                
            case NEVER:
                if (currentStatus != null && !currentStatus.isCompleted()) {
                    throw new RuntimeException("当前存在事务，但传播行为为NEVER");
                }
                return createNonTransactionalStatus();
                
            case MANDATORY:
                if (currentStatus == null || currentStatus.isCompleted()) {
                    throw new RuntimeException("当前不存在事务，但传播行为为MANDATORY");
                }
                return currentStatus;
                
            default:
                return createNewTransaction(isolation);
        }
    }
    
    /**
     * 创建新事务
     */
    private static TransactionStatus createNewTransaction(Isolation isolation) throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        connection.setAutoCommit(false);
        
        // 设置隔离级别
        setIsolationLevel(connection, isolation);
        
        TransactionStatus status = new TransactionStatus(connection, true);
        transactionStack.get().push(status);
        return status;
    }
    
    /**
     * 创建非事务状态
     */
    private static TransactionStatus createNonTransactionalStatus() throws SQLException {
        Connection connection = DatabaseUtil.getConnection();
        connection.setAutoCommit(true);
        
        TransactionStatus status = new TransactionStatus(connection, false);
        transactionStack.get().push(status);
        return status;
    }
    
    /**
     * 设置隔离级别
     */
    private static void setIsolationLevel(Connection connection, Isolation isolation) throws SQLException {
        switch (isolation) {
            case READ_UNCOMMITTED:
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                break;
            case READ_COMMITTED:
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                break;
            case REPEATABLE_READ:
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                break;
            case SERIALIZABLE:
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                break;
            case DEFAULT:
            default:
                // 使用数据库默认隔离级别
                break;
        }
    }
    
    /**
     * 提交事务
     */
    public static void commit(TransactionStatus status) throws SQLException {
        if (status.isNewTransaction() && !status.isCompleted()) {
            try {
                if (status.isRollbackOnly()) {
                    status.getConnection().rollback();
                } else {
                    status.getConnection().commit();
                }
            } finally {
                status.setCompleted();
                closeConnection(status);
                transactionStack.get().pop();
            }
        }
    }
    
    /**
     * 回滚事务
     */
    public static void rollback(TransactionStatus status) throws SQLException {
        if (status.isNewTransaction() && !status.isCompleted()) {
            try {
                status.getConnection().rollback();
            } finally {
                status.setCompleted();
                closeConnection(status);
                transactionStack.get().pop();
            }
        } else if (!status.isNewTransaction()) {
            // 如果不是新事务，标记为回滚
            status.setRollbackOnly();
        }
    }
    
    /**
     * 获取当前事务状态
     */
    public static TransactionStatus getCurrentTransaction() {
        Stack<TransactionStatus> stack = transactionStack.get();
        return stack.isEmpty() ? null : stack.peek();
    }
    
    /**
     * 关闭连接
     */
    private static void closeConnection(TransactionStatus status) {
        try {
            if (status.getConnection() != null && !status.getConnection().isClosed()) {
                status.getConnection().close();
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理当前线程的事务状态
     */
    public static void cleanup() {
        transactionStack.remove();
    }
}