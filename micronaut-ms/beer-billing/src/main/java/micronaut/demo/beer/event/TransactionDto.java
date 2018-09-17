package micronaut.demo.beer.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import micronaut.demo.beer.model.Ticket;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    String username;
    Ticket ticket;
    public TransactionDto(String username,Ticket ticket ) {
        this.username=username;
        this.ticket=ticket;
    }
    public void setUsername(String customerName) {
        this.username=customerName;
    }

    public void setTicket(Ticket ticket) {
        this.ticket=ticket;
    }

    public Ticket getTicket() {
        return this.ticket;
    }
    public String getUsername() {
        return this.username;
    }
}

