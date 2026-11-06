package com.example.banking.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="ledger_entries", indexes=@Index(name="idx_ledger_account_time", columnList="account_id,occurred_at"))
public class LedgerEntry {
 @Id private UUID id;
 @Column(name="account_id", nullable=false) private String accountId;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private TransactionType type;
 @Column(nullable=false,precision=19,scale=2) private BigDecimal amount;
 @Column(name="balance_after",nullable=false,precision=19,scale=2) private BigDecimal balanceAfter;
 @Column(name="occurred_at",nullable=false) private Instant occurredAt;
 @Column(name="transfer_id") private UUID transferId;
 protected LedgerEntry() {}
 public LedgerEntry(String accountId, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, Instant occurredAt, UUID transferId) {
  this.id=UUID.randomUUID(); this.accountId=accountId; this.type=type; this.amount=amount;
  this.balanceAfter=balanceAfter; this.occurredAt=occurredAt; this.transferId=transferId;
 }
 public UUID getId(){return id;} public String getAccountId(){return accountId;}
 public TransactionType getType(){return type;} public BigDecimal getAmount(){return amount;}
 public BigDecimal getBalanceAfter(){return balanceAfter;} public Instant getOccurredAt(){return occurredAt;}
 public UUID getTransferId(){return transferId;}
}
