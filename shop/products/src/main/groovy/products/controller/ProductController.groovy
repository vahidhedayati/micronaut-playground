package products.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import products.domain.Chair
import products.domain.Product
import products.domain.Table
import products.service.ProductService

@Controller("/")
@CompileStatic
class ProductController {
    final ProductService productService

    ProductController(ProductService productService) {
        this.productService = productService
    }

    @Get("/")
    List<Product> list() {
        return productService.findAll()
    }

    @Get("/chairs")
    List<Chair> listChairs() {
        return productService.findAllChairs()
    }

    @Get("/tables")
    List<Table> listTables() {
        return productService.findAllTables()
    }

    @Get("/{id}")
    Product show(Long id) {
        return productService.find(id)
    }
}