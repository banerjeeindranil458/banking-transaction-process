package com.example.banking.api;
import com.example.banking.api.Requests.*;
import com.example.banking.api.Responses.*;
import com.example.banking.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api")
public class BankingController {
 private final BankingService service;
 public BankingController(BankingService service){this.service=service;}
 @PostMapping("/accounts") public ResponseEntity<AccountView> create(@Valid @RequestBody CreateAccount request){
  return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(request.id()));
 }
 @GetMapping("/accounts/{id}") public AccountView get(@PathVariable String id){return service.getAccount(id);}
 @GetMapping("/accounts/{id}/transactions") public List<LedgerView> history(@PathVariable String id){return service.history(id);}
 @PostMapping("/accounts/{id}/deposits") public AccountView deposit(@PathVariable String id,@Valid @RequestBody Amount request){return service.deposit(id,request.amount());}
 @PostMapping("/accounts/{id}/withdrawals") public AccountView withdraw(@PathVariable String id,@Valid @RequestBody Amount request){return service.withdraw(id,request.amount());}
 @PostMapping("/transfers") public TransferView transfer(@Valid @RequestBody Transfer request){return service.transfer(request.fromAccountId(),request.toAccountId(),request.amount());}
}
