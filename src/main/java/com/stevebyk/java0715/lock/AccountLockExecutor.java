package com.stevebyk.java0715.lock;

import java.util.Collection;
import java.util.function.Supplier;

public interface AccountLockExecutor {

    <T> T executeWithAccountLocks(Collection<String> accountNos, Supplier<T> action);
}
