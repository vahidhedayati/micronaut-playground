package products.service

import grails.gorm.services.Service
import products.domain.Chair
import products.domain.Product
import products.domain.Table

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(Product)
interface ProductService {
    Product save(Product product)
    Product save(@NotBlank String name,@NotBlank String description)
    Table save(Table Table)
    Chair save(@Valid Chair chair)
    List<Product> findAll()
    Number count()
    Product find(@NotNull Long id)
    Table findTable(@NotNull Long id)
    Chair findChair(@NotNull Long id)

}