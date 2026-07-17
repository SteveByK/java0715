package com.stevebyk.java0715.account;

import com.stevebyk.java0715.common.ddd.OutboundPort;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@OutboundPort
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNo(String accountNo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from AccountEntity account where account.accountNo = :accountNo")
    Optional<AccountEntity> findByAccountNoForUpdate(@Param("accountNo") String accountNo);
}
