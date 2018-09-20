package beer.websocket;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;

import javax.validation.constraints.NotBlank;

@Controller("/beersock")
@Validated
public class SocketController {

    /**
     * This doesn't need to send anything back it itself triggers sockets to forward the message
     * this is now using consul technology which picks given socket server that broadcasts a given
     * order that comes in string format {customername:beerName:beerSize}
     * @param order
     * @return
     */
    @Post("/addBeer/{order}")
    public HttpResponse<String> addBeerToCustomerBill(@NotBlank String order) {
        System.out.println("BROADCASTING ORDER"+order);
        TransactionWebSocket.myBroadCast(order);
        return HttpResponse.ok("200");
    }
}
