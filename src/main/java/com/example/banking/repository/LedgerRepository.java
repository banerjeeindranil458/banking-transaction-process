package com.example.banking.repository;
import com.example.banking.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface LedgerRepository extends JpaRepository<LedgerEntry,UUID> {
 List<LedgerEntry> findByAccountIdOrderByOccurredAtAscIdAsc(String accountId);
}
