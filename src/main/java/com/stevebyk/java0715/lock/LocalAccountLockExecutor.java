package com.stevebyk.java0715.lock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class LocalAccountLockExecutor implements AccountLockExecutor {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public <T> T executeWithAccountLocks(Collection<String> accountNos, Supplier<T> action) {
        List<String> orderedAccountNos = accountNos.stream().distinct().sorted().toList();
        List<ReentrantLock> orderedLocks = orderedAccountNos.stream()
                .map(accountNo -> locks.computeIfAbsent(accountNo, ignored -> new ReentrantLock()))
                .toList();
        orderedLocks.forEach(ReentrantLock::lock);
        try {
            return action.get();
        } finally {
            for (int index = orderedLocks.size() - 1; index >= 0; index--) {
                orderedLocks.get(index).unlock();
            }
        }
    }
}
