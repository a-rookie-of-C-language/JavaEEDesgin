package site.arookieofc.processor.transaction;

import lombok.Getter;
import java.sql.Connection;

/**
 * 事务状态
 */
@Getter
public class TransactionStatus {
    private final Connection connection;
    private final boolean newTransaction;
    private boolean rollbackOnly;
    private boolean completed;

    public TransactionStatus(Connection connection, boolean newTransaction) {
        this.connection = connection;
        this.newTransaction = newTransaction;
        this.rollbackOnly = false;
        this.completed = false;
    }

    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    public void setCompleted() {
        this.completed = true;
    }

    public boolean isTransactional(){
        return this.newTransaction;
    }
}