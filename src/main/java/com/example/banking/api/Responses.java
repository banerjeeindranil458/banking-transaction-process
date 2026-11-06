package com.example.banking.api;
import com.example.banking.domain.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
public final class Responses {
 private Responses() {}
 public record AccountView(String id, BigDecimal balance) {
  public static AccountView of(Account a){return new AccountView(a.getId(),a.getBalance());}
 }
 public record LedgerView(UUID id, String accountId, TransactionType type, BigDecimal amount,
    BigDecimal balanceAfter, Instant occurredAt, UUID transferId) {
  public static LedgerView of(LedgerEntry e){return new LedgerView(e.getId(),e.getAccountId(),e.getType(),e.getAmount(),e.getBalanceAfter(),e.getOccurredAt(),e.getTransferId());}
 }
 public record TransferView(UUID transferId, AccountView from, AccountView to) {}
}
