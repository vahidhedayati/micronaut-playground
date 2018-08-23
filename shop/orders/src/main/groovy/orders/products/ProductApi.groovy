package orders.products

import io.micronaut.http.annotation.Get
import io.reactivex.Maybe
import orders.users.User

interface ProductApi {
    @Get("/{id}")
    Maybe<Product> findProduct(Long id)

    @Get("/parseIds/{ids}")
    List<Product> findProductBatch(List<Long> ids)
}