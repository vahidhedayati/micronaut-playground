package micronaut.demo.beer.kafka;

import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.KafkaKey;
import io.micronaut.configuration.kafka.annotation.Topic;
import micronaut.demo.beer.event.TransactionRegisterEvent;
import micronaut.demo.beer.event.TransactionTerminationEvent;

@KafkaClient
public interface EventPublisher {

    @Topic("transcation-registered")
    void  transactionRegisteredEvent(@KafkaKey String username, TransactionRegisterEvent event);

    @Topic("transcation-registered3")
    void  transactionRegEvent(TransactionRegisterEvent event);

    @Topic("transaction-terminated")
    void transactionTerminatedEvent(@KafkaKey String username, TransactionTerminationEvent event);
}
