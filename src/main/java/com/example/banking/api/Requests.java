package com.example.banking.api;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public final class Requests {
 private Requests() {}
 public record CreateAccount(@NotBlank String id) {}
 public record Amount(@NotNull @DecimalMin(value="0.00",inclusive=false) @Digits(integer=17,fraction=2) BigDecimal amount) {}
 public record Transfer(@NotBlank String fromAccountId, @NotBlank String toAccountId,
   @NotNull @DecimalMin(value="0.00",inclusive=false) @Digits(integer=17,fraction=2) BigDecimal amount) {}
}
