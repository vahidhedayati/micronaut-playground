package micronaut.demo.beer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.sse.Event;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.tracing.annotation.ContinueSpan;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.micronaut.validation.Validated;
import io.reactivex.Flowable;
import io.reactivex.Single;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;
import micronaut.demo.beer.service.BillService;
import micronaut.demo.beer.service.CostCalculator;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Optional;


@Controller("/billing")
@Validated
public class TicketController {

	final EmbeddedServer embeddedServer;
	final CostCalculator beerCostCalculator;
	final BillService billService;

	@Inject
	public TicketController(EmbeddedServer embeddedServer,
							CostCalculator beerCostCalculator,
							BillService billService) {
		this.embeddedServer = embeddedServer;
		this.beerCostCalculator = beerCostCalculator;
		this.billService = billService;
	}
	
	@Get("/reset/{customerName}")
    public HttpResponse resetCustomerBill(@NotBlank String customerName) {
			billService.createBillForCostumer(customerName, null);
    	    return HttpResponse.ok();
    }

	@Post("/addBeer/{customerName}")
	public HttpResponse<BeerItem> addBeerToCustomerBill(@Body BeerItem beer, @NotBlank String customerName) {


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
					final String url = "ws://"+hostPort+"/ws/";
					System.out.println("About to connect to billing server for beer-websocket running on "+url);
					try {
						WebSocketClient client=new WebSocketClient(url,billService);
						client.open();
						client.eval(customerName+":"+beer.getName()+":"+beer.getSize().toString());
						client.close();
					} catch (Exception e) {

					}


				});

		return HttpResponse.ok(beer);
	}


	@Get("/bill/{customerName}")
	@NewSpan("bill")
    public Single<Ticket> bill(@NotBlank String customerName) {
			Optional<Ticket> t = getTicketForUser(customerName);
    		Ticket ticket = t.isPresent() ?  t.get() : new Ticket();
    		ticket.setDeskId(embeddedServer.getPort());
        return Single.just(ticket);
    }

	@Get("/cost/{customerName}")
	@NewSpan("cost")
	public Single<Double> cost(@NotBlank String customerName) {
		Optional<Ticket> t = getTicketForUser(customerName);
		double cost = t.isPresent() ? beerCostCalculator.calculateCost(t.get()) :
										  beerCostCalculator.calculateCost(getNoCostTicket());

		return Single.just(Double.valueOf(cost));
	}

	@Get(uri = "/users", produces = MediaType.TEXT_EVENT_STREAM)
	Publisher<Event<String>> users() {
		return Flowable.generate(() -> 0, (i, emitter) -> {
			if (i < 100000) {
				Thread.sleep(200);
				emitter.onNext(Event.of(billService.usersInBarMessage()));
			} else {
				emitter.onComplete();
			}
			return ++i;
		});
	}

	@ContinueSpan
	private Ticket getNoCostTicket() {
		BeerItem smallBeer = new BeerItem("Korona", BeerItem.Size.EMPTY);
		Ticket noCost = new Ticket();
		noCost.add(smallBeer);
		return noCost;
	}

	@ContinueSpan
	private Optional<Ticket> getTicketForUser(@SpanTag("getTicketForUser") String customerName) {
		return Optional.ofNullable(billService.getBillForCostumer(customerName));
	}
}
