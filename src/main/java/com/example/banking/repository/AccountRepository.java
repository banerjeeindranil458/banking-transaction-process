package com.example.banking.repository;
import com.example.banking.domain.Account;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface AccountRepository extends JpaRepository<Account,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select a from Account a where a.id = :id")
 Optional<Account> findLockedById(@Param("id") String id);
}
