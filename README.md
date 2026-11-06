# Banking Transaction Processor

A small Java 17 / Spring Boot 3 REST service implementing the **Banking Transaction Processor** kata.

## Problem understanding

An account has a unique client-supplied string ID and a nonnegative balance. Deposits and withdrawals affect one account. A transfer must debit one account and credit another **atomically**. Each balance-changing operation creates immutable ledger records with UTC timestamps and resulting balances. Transfers create two records sharing a transfer ID.

## Run

Prerequisites: Java 17+ and Maven 3.9+.

```bash
mvn clean test
mvn spring-boot:run
```

Service: `http://localhost:8080`. The H2 database is in memory and is reset when the application restarts.

## REST endpoints

| Method | Path | JSON request | Response |
|---|---|---|---|
| POST | `/api/accounts` | `{"id":"alice"}` | 201 account |
| GET | `/api/accounts/{id}` | — | 200 account |
| GET | `/api/accounts/{id}/transactions` | — | 200 ordered ledger array |
| POST | `/api/accounts/{id}/deposits` | `{"amount":100.00}` | 200 account |
| POST | `/api/accounts/{id}/withdrawals` | `{"amount":30.00}` | 200 account |
| POST | `/api/transfers` | `{"fromAccountId":"alice","toAccountId":"bob","amount":20.00}` | 200 transfer result |

### Try it

```bash
curl -i -X POST localhost:8080/api/accounts -H 'Content-Type: application/json' -d '{"id":"alice"}'
curl -i -X POST localhost:8080/api/accounts -H 'Content-Type: application/json' -d '{"id":"bob"}'
curl -i -X POST localhost:8080/api/accounts/alice/deposits -H 'Content-Type: application/json' -d '{"amount":100.00}'
curl -i -X POST localhost:8080/api/transfers -H 'Content-Type: application/json' -d '{"fromAccountId":"alice","toAccountId":"bob","amount":25.00}'
curl localhost:8080/api/accounts/alice
curl localhost:8080/api/accounts/bob/transactions
```

Expected final balances: Alice `75.00`, Bob `25.00`.

### Response examples

Account: `{"id":"alice","balance":75.00}`

Ledger item: `{"id":"<uuid>","accountId":"alice","type":"TRANSFER_OUT","amount":25.00,"balanceAfter":75.00,"occurredAt":"<UTC timestamp>","transferId":"<uuid>"}`

Errors use HTTP 400 for invalid input, 404 for missing accounts, and 409 for insufficient funds or duplicate IDs. Errors include an `error` message. Spring's default handling applies to unexpected failures.

## Design choices

- **Money:** `BigDecimal` with two decimal places, not floating point. All amounts must be strictly positive and fit `DECIMAL(19,2)`; API validation rejects extra fractional digits.
- **Object orientation:** `Account` owns its balance mutations; `BankingService` orchestrates use cases; repositories handle persistence; controller translates HTTP.
- **Atomicity:** `@Transactional` encloses both balance updates and both transfer ledger inserts. An exception rolls back the operation.
- **Concurrency:** Pessimistic row locks serialize updates to accounts; transfers acquire locks in lexicographic ID order to reduce deadlocks.
- **Auditability:** Ledger records store positive amount, type, resulting balance, timestamp, and shared transfer ID. A transfer produces a `TRANSFER_OUT` and a `TRANSFER_IN` record.
- **Simplicity:** An in-memory H2 database avoids deployment overhead for this timeboxed kata.

## Tests

`BankingIntegrationTest` exercises REST requests and persistence end-to-end:

1. Deposit and withdrawal update balance and append timestamped ledger entries.
2. Transfer moves money and creates linked debit/credit entries.
3. Insufficient funds reject a transfer without changing balances or ledger.
4. Zero, negative, over-precision amounts and self-transfers are rejected.
5. Duplicate IDs and nonexistent accounts return appropriate statuses.

Run with `mvn test`.

## Limitations and next iterations

- **No authentication or authorization:** This is a kata, not a production banking service. Add account-level access controls before deployment.
- **In-memory storage:** Data is not durable across restarts. Use PostgreSQL and migrations (Flyway) in production.
- **No idempotency keys:** A retried POST could apply an operation twice. Add client operation IDs with a unique constraint and replay-safe responses.
- **No pagination:** Large histories should use cursor-based pagination and stable ordering.
- **No concurrency stress tests:** Add simultaneous withdrawals/transfers using multiple threads and verify no overdrafts or deadlocks on the chosen production database.
- **No currency field:** Assumes a single currency and 2 fractional digits; multi-currency transfers need explicit conversion rules.
- **No overdraft or account lifecycle support:** Balances start at zero; accounts cannot be closed.
- **Audit ledger is append-only by convention, not database permissions:** Production audit integrity needs tighter guarantees.
- **No API versioning, metrics, tracing, or rate limiting:** These are intentionally outside the 3–4 hour scope.

## Iteration notes

Suggested test-driven progression: account creation → deposit → withdrawal rejection → transfer → history → HTTP validation → concurrency locking. Each stage should be committed separately. This repository contains the final snapshot; do **not** present a fabricated commit history as actual development history.

