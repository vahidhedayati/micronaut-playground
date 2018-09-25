package micronaut.demo.beer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import micronaut.demo.beer.client.TicketControllerClient;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Controller("/waiter")
@Validated
public class WaiterController {

    TicketControllerClient ticketControllerClient;

    final EmbeddedServer embeddedServer;

    @Inject
    public WaiterController(TicketControllerClient ticketControllerClient,EmbeddedServer embeddedServer) {
        this.ticketControllerClient = ticketControllerClient;
        this.embeddedServer=embeddedServer;
    }

    @Get("/beer/{customerName}")
    //@NewSpan
    public Single<Beer> serveBeerToCustomer(@NotBlank String customerName) {
        Beer beer = new Beer("mahou", Beer.Size.MEDIUM);
        BeerItem beerItem = new BeerItem(beer.getName(), BeerItem.Size.MEDIUM);
        ConsulClient client1 = new ConsulClient("localhost");
        Response<List<HealthService>> healthyServices = client1.getHealthServices("billing", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service =healthService.getService();
                    // if (embeddedServer.getPort()!=service.getPort()) {}

                    //This now connects back to all running instances of websocket server
                    //Sends through the current port of this application as well as its name making up most of the ws://{host}:{port} up
                    //otherside simply triggers socket connection to it

                    String hostPort = service.getAddress() + ":" + service.getPort();
                    System.out.print(hostPort);
                    final String url = "ws://"+hostPort+"/ws/"+embeddedServer.getHost()+":"+embeddedServer.getPort();
                    //System.out.println("About to connect to billing server for beer-websocket running on "+url);
                    try {
                        WebSocketClient client=new WebSocketClient(url);
                        client.open();
                        client.eval(customerName+":"+beer.getName()+":"+beer.getSize().toString());
                        client.close();
                    } catch (Exception e) {

                    }


                });
        //ticketControllerClient.addBeerToCustomerBill(beerItem, customerName);
        return Single.just(beer);
    }
    
    @Get("/bill/{customerName}")
    //@NewSpan
    public Single<CustomerBill> bill(@NotBlank String customerName) {
        Single<Ticket> singleTicket = ticketControllerClient.bill(customerName);
        Single<Double> singleCost= ticketControllerClient.cost(customerName);
        Ticket ticket= singleTicket.blockingGet();
        CustomerBill bill = new CustomerBill(singleCost.blockingGet().doubleValue());
        bill.setDeskId(ticket.getDeskId());
        return Single.just(bill);
    }
}
