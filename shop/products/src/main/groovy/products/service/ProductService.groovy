package products.service

import grails.gorm.services.Service
import products.domain.Chair
import products.domain.Product
import products.domain.Table

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service(Product)
abstract class ProductService {

    abstract Product save(@Valid Product product)
    abstract Table save(@Valid Table Table)
    abstract Chair save(@Valid Chair chair)

    abstract List<Product> findAll()
    abstract List<Table> findAllTables()
    abstract List<Chair> findAllChairs()


    abstract Number count()


    abstract Product find(@NotNull Long id)
    abstract Table findTable(@NotNull Long id)
    abstract Chair findChair(@NotNull Long id)

}