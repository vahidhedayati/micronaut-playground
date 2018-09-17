package micronaut.demo.beer.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import lombok.RequiredArgsConstructor;
import micronaut.demo.beer.event.TransactionRegisterEvent;
import micronaut.demo.beer.model.Ticket;

@RequiredArgsConstructor
@KafkaListener//(offsetReset = OffsetReset.EARLIEST)
public class TransactionRegisteredListener {


    @Topic("transaction-registered")
    void onPolicyRegistered(TransactionRegisterEvent event) {
        System.out.println("We got a transaction event \n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
        Ticket ticket = event.getTransaction().getTicket();

        System.out.println("---------------------------WE GOT TICKET "+ticket);
    }


}
