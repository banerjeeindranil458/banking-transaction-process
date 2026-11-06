package com.example.banking.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="accounts")
public class Account {
 @Id @Column(nullable=false, updatable=false) private String id;
 @Column(nullable=false, precision=19, scale=2) private BigDecimal balance;
 protected Account() {}
 public Account(String id) { this.id=id; this.balance=new BigDecimal("0.00"); }
 public String getId(){return id;}
 public BigDecimal getBalance(){return balance;}
 public void deposit(BigDecimal amount){ balance=balance.add(amount); }
 public void withdraw(BigDecimal amount){
  if(balance.compareTo(amount)<0) throw new IllegalStateException("Insufficient funds");
  balance=balance.subtract(amount);
 }
}
