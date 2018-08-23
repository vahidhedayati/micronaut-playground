package orders.products

import io.micronaut.http.client.Client

@Client("products")
interface ProductClient extends ProductApi {

}