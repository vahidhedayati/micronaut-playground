package micronaut.demo.beer.client;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;


import javax.validation.constraints.NotBlank;

@Client(id = "beersocket", path = "/beersock")
public interface SocketControllerClient {


    @Post("/addBeer/{order}")
    HttpResponse<String> addBeerToCustomerBill(@NotBlank String order);



}


