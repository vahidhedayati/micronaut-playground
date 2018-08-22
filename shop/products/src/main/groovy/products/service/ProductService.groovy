package products.service

import grails.gorm.services.Service
import products.domain.Product

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(Product)
interface ProductService {
    Product save(@NotBlank String name,@NotBlank String title)
    List<Product> findAll()
    Number count()
    Product find(@NotNull Long id)

}