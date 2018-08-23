package orders.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.Client
import orders.view.OrderView

@Client("/")
interface OrderClient {

    @Get("/")
    List<OrderView> findAll()

}