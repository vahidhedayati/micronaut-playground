package micronaut.demo.beer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTerminationEvent {

   TransactionDto transaction;
}
