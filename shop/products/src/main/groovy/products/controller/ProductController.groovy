package products.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import products.domain.Chair
import products.domain.Product
import products.domain.ShopTable
import products.service.ChairService
import products.service.ProductService
import products.service.ShopTableService

@Controller("/")


class ProductController {
    final ProductService productService
    final ChairService chairService
    final ShopTableService shopTableService

    ProductController(ProductService productService) {
        this.productService = productService
    }

    ProductController(ChairService chairService) {
        this.chairService = chairService
    }

    ProductController(ShopTableService shopTableService) {
        this.shopTableService = shopTableService
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
    List<ShopTable> listTables() {
        return shopTableService.findAll()
    }

    @Get("/{id}")
    Product show(Long id) {
        return productService.find(id)
    }

    @Get("/parseIds/{ids}")
    List parseIds(List<Long>  ids) {
        return productService.findBatch(ids)
    }
}