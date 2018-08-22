package products.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import products.domain.Chair
import products.domain.Product
import products.domain.Table
import products.service.ChairService
import products.service.ProductService
import products.service.TableService

@Controller("/")


class ProductController {
    final ProductService productService
    final ChairService chairService
    final TableService tableService

    ProductController(ProductService productService) {
        this.productService = productService
    }

    ProductController(ChairService chairService) {
        this.chairService = chairService
    }

    ProductController(TableService tableService) {
        this.tableService = tableService
    }

    @Get("/")
    List<Product> list() {
        return productService.findAll()
    }

    @Get("/chairs")
    List<Chair> listChairs() {
        return chairService.findAll()
    }

    @Get("/tables")
    List<Table> listTables() {
        return tableService.findAll()
    }

    @Get("/{id}")
    Product show(Long id) {
        return productService.find(id)
    }
}