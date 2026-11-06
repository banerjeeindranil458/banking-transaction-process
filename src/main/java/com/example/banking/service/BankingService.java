package com.example.banking.service;
import com.example.banking.api.Responses.*;
import com.example.banking.domain.*;
import com.example.banking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
@Service public class BankingService {
 private final AccountRepository accounts;
 private final LedgerRepository ledger;
 private final Clock clock;
@Autowired
 public BankingService(AccountRepository accounts, LedgerRepository ledger) {
  this(accounts,ledger,Clock.systemUTC());
 }
 // Package-private constructor makes deterministic clock injection possible in focused tests.
 BankingService(AccountRepository accounts, LedgerRepository ledger, Clock clock) {
  this.accounts=accounts; this.ledger=ledger; this.clock=clock;
 }
 @Transactional public AccountView createAccount(String id) {
  if(id==null || id.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account ID is required");
  if(accounts.existsById(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Account already exists");
  return AccountView.of(accounts.saveAndFlush(new Account(id)));
 }
 @Transactional(readOnly=true) public AccountView getAccount(String id) {return AccountView.of(find(id));}
 @Transactional(readOnly=true) public List<LedgerView> history(String id) {
  find(id);
  return ledger.findByAccountIdOrderByOccurredAtAscIdAsc(id).stream().map(LedgerView::of).toList();
 }
 @Transactional public AccountView deposit(String id, BigDecimal amount) {
  validate(amount);
  Account a=locked(id); a.deposit(amount);
  ledger.save(new LedgerEntry(id,TransactionType.DEPOSIT,amount,a.getBalance(),Instant.now(clock),null));
  return AccountView.of(a);
 }
 @Transactional public AccountView withdraw(String id, BigDecimal amount) {
  validate(amount);
  Account a=locked(id); ensureFunds(a,amount); a.withdraw(amount);
  ledger.save(new LedgerEntry(id,TransactionType.WITHDRAWAL,amount,a.getBalance(),Instant.now(clock),null));
  return AccountView.of(a);
 }
 @Transactional public TransferView transfer(String fromId, String toId, BigDecimal amount) {
  validate(amount);
  if(fromId.equals(toId)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cannot transfer to the same account");
  // Lock in stable ID order to reduce the risk of deadlocks in opposite-direction transfers.
  String first=fromId.compareTo(toId)<0?fromId:toId;
  String second=fromId.compareTo(toId)<0?toId:fromId;
  Account a=locked(first); Account b=locked(second);
  Account from=fromId.equals(first)?a:b; Account to=toId.equals(second)?b:a;
  ensureFunds(from,amount);
  from.withdraw(amount); to.deposit(amount);
  UUID transferId=UUID.randomUUID(); Instant timestamp=Instant.now(clock);
  ledger.save(new LedgerEntry(fromId,TransactionType.TRANSFER_OUT,amount,from.getBalance(),timestamp,transferId));
  ledger.save(new LedgerEntry(toId,TransactionType.TRANSFER_IN,amount,to.getBalance(),timestamp,transferId));
  return new TransferView(transferId,AccountView.of(from),AccountView.of(to));
 }
 private Account find(String id){return accounts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found: "+id));}
 private Account locked(String id){return accounts.findLockedById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found: "+id));}
 private static void ensureFunds(Account a, BigDecimal amount){
  if(a.getBalance().compareTo(amount)<0) throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient funds");
 }
 private static void validate(BigDecimal amount){
  if(amount==null || amount.signum()<=0 || amount.scale()>2 || amount.precision()-amount.scale()>17)
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Amount must be positive with at most two decimal places and 17 integer digits");
 }
}
