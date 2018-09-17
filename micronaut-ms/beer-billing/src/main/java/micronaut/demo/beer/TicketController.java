package micronaut.demo.beer;

import io.micronaut.context.ApplicationContext;
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
import micronaut.demo.beer.event.TransactionDto;
import micronaut.demo.beer.event.TransactionRegisterEvent;
import micronaut.demo.beer.kafka.EventPublisher;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;
import micronaut.demo.beer.service.BillService;
import micronaut.demo.beer.service.CostCalculator;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import java.util.Optional;

//import io.micronaut.tracing.annotation.ContinueSpan;
//import io.micronaut.tracing.annotation.NewSpan;


@Controller("/billing")
@Validated
public class TicketController {

	final EmbeddedServer embeddedServer;
	final CostCalculator beerCostCalculator;
	final BillService billService;
	//static ApplicationContext applicationContext;
	EventPublisher eventPublisher;

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
    	    Optional<Ticket> t = getTicketForUser(customerName);
    	    Ticket ticket = t.isPresent() ?  t.get() : new Ticket();
    	    ticket.add(beer);


    	    try {

				//EventPublisher client = applicationContext.getBean(EventPublisher.class);
				eventPublisher.transactionRegisteredEvent(customerName, createEvent(ticket, customerName));
			} catch (Exception e) {
				e.printStackTrace();
				//System.out.println("Errror s \n\n\n\n\n\n\n\n\n\n\n" + );

			}


			billService.createBillForCostumer(customerName, ticket);

    	    return HttpResponse.ok(beer);
    }
	private TransactionRegisterEvent createEvent(Ticket ticket, String username) {
		return new TransactionRegisterEvent(new TransactionDto(username, ticket));
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
