package micronaut.demo.beer.service;

import lombok.RequiredArgsConstructor;
import micronaut.demo.beer.event.TransactionDto;
import micronaut.demo.beer.event.TransactionRegisterEvent;
import micronaut.demo.beer.model.Ticket;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
public class MapBillService implements BillService {

    //EventPublisher eventPublisher;
  //  static ApplicationContext applicationContext;

    Map<String, Ticket> billsPerCustomer = new HashMap<>();
    public Ticket getBillForCostumer(String username) { return billsPerCustomer.get(username); }

    public void  createBillForCostumer(String username, Ticket ticket) {

        //System.out.println ("Kafka service call success \n\n\n\n\n\n\n\n\n\n");
        /*TransactionDto dto = new TransactionDto();
        dto.setUsername(username);
        dto.setTicket(ticket);

        TransactionRegisterEvent tr = new TransactionRegisterEvent();
        tr.setTransaction(dto);
        */
        //System.out.println ("Customer name  "+username+"-0-------------------------------------");
        // TransactionRegisterEvent eve = createEvent(ticket,username);
        // System.out.println ("Customer name 222 "+username+"-0-------------------------------------"+eve);
        //eventPublisher.transactionRegEvent(tr);

        //EventPublisher client = applicationContext.getBean(EventPublisher.class);
        //client.transactionRegisteredEvent(username, eve);
        //  eventPublisher.transactionRegisteredEvent(username, eve);

        billsPerCustomer.put(username,ticket);


    };

    private TransactionRegisterEvent createEvent(Ticket ticket, String username) {
        return new TransactionRegisterEvent(new TransactionDto(username, ticket));
    }

    public String usersInBarMessage() {
        int howManyUsers = size();
        if (howManyUsers==0) {
            return "The bar is empty!";
        } else {
            String users = billsPerCustomer.keySet().stream().collect(Collectors.joining(","));
            return "The bar has " + howManyUsers + " users: " + users;
        }
    }

    private int size() {
        return billsPerCustomer.size();
    }


}
