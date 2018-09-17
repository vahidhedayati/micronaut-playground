package micronaut.demo.beer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRegisterEvent {

   TransactionDto transaction;

   public TransactionRegisterEvent (TransactionDto transaction) {
      this.transaction=transaction;
   }
   public void setTransaction(TransactionDto dto) {
      this.transaction=dto;
   }

   public TransactionDto getTransaction() {
      return this.transaction;
   }
}
