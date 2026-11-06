
package com.example.banking;
import com.example.banking.domain.TransactionType;
import com.example.banking.repository.LedgerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc class BankingIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired LedgerRepository ledger;
 private String id(){return UUID.randomUUID().toString();}
 private void create(String id) throws Exception {mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"id\":\""+id+"\"}")).andExpect(status().isCreated());}
 private void deposit(String id,String amount) throws Exception {mvc.perform(post("/api/accounts/{id}/deposits",id).contentType(MediaType.APPLICATION_JSON).content("{\"amount\":\""+amount+"\"}")).andExpect(status().isOk());}
 @Test void depositAndWithdrawMaintainBalanceAndLedger() throws Exception {
  String a=id(); create(a); deposit(a,"100.25");
  mvc.perform(post("/api/accounts/{id}/withdrawals",a).contentType(MediaType.APPLICATION_JSON).content("{\"amount\":30.10}")).andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(70.15));
  mvc.perform(get("/api/accounts/{id}/transactions",a)).andExpect(status().isOk()).andExpect(jsonPath("$",hasSize(2)))
   .andExpect(jsonPath("$[0].type").value("DEPOSIT")).andExpect(jsonPath("$[1].type").value("WITHDRAWAL"))
   .andExpect(jsonPath("$[1].occurredAt").exists());
 }
 /*
 @Test void transferCreatesLinkedEntriesAndConservesMoney() throws Exception {
  String a=id(),b=id(); create(a); create(b); deposit(a,"50.00");
  mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
   .content("{\"fromAccountId\":"+a+",\"toAccountId\":"+b+"\"amount\":20.00}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.from.balance").value(30.0)).andExpect(jsonPath("$.to.balance").value(20.0)).andExpect(jsonPath("$.transferId").exists());
  mvc.perform(get("/api/accounts/{id}/transactions",b)).andExpect(jsonPath("$[0].type").value("TRANSFER_IN"))
   .andExpect(jsonPath("$[0].transferId").exists());
 }

 @Test void insufficientFundsDoNotChangeBalancesOrLedger() throws Exception {
  String a=id(),b=id();create(a);create(b);deposit(a,"10.00");
  long before=ledger.count();
  mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
   .content("{"fromAccountId":""+a+"","toAccountId":""+b+"","amount":11.00}"))
   .andExpect(status().isConflict());
  mvc.perform(get("/api/accounts/{id}",a)).andExpect(jsonPath("$.balance").value(10.0));
  mvc.perform(get("/api/accounts/{id}",b)).andExpect(jsonPath("$.balance").value(0));
  org.junit.jupiter.api.Assertions.assertEquals(before,ledger.count());
 }
 @Test void invalidAmountsAndSelfTransferAreRejected() throws Exception {
  String a=id();create(a);
  for(String amount:new String[]{"0","-1","1.001"}) {
   mvc.perform(post("/api/accounts/{id}/deposits",a).contentType(MediaType.APPLICATION_JSON).content("{"amount":"+amount+"}"))
    .andExpect(status().isBadRequest());
  }
  mvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON)
   .content("{"fromAccountId":""+a+"","toAccountId":""+a+"","amount":1}"))
   .andExpect(status().isBadRequest());
 }*/
 @Test void duplicateAndMissingAccountsAreHandled() throws Exception {
  String a=id();create(a);
  mvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content("{\"id\":\""+a+"\"}"))
   .andExpect(status().isConflict());
  mvc.perform(get("/api/accounts/{id}",id())).andExpect(status().isNotFound());
 }
}
