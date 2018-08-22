package products.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import products.domain.ProductNonExtended
import products.service.ProductNonExtendedService

@Controller("/nonExtended")


class ProductNonExtendedController {
    final ProductNonExtendedService productNonExtendedService


    ProductNonExtendedController(ProductNonExtendedService productNonExtendedService) {
        this.productNonExtendedService = productNonExtendedService
    }


    @Get("/")
    List<ProductNonExtended> list() {
        List<ProductNonExtended> results = productNonExtendedService.findAll()
        println "-working on listing ${results.size()}"
        return results
    }


    @Get("/{id}")
    ProductNonExtended show(Long id) {
        return productNonExtendedService.find(id)
    }
}