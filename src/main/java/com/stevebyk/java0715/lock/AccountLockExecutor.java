package com.stevebyk.java0715.lock;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Account-level lock abstraction used by money movement orchestration.
 */
public interface AccountLockExecutor {

    /**
     * Executes an action while holding account locks for all provided account numbers.
     */
    <T> T executeWithAccountLocks(Collection<String> accountNos, Supplier<T> action);
}
