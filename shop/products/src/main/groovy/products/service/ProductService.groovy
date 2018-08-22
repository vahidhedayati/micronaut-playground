package products.service

import grails.gorm.services.Service
import products.domain.Chair
import products.domain.Product
import products.domain.ShopTable

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(Product)
abstract class ProductService {

    abstract Product save(@Valid Product product)
    abstract ShopTable save(@Valid ShopTable shopTable)
    abstract Chair save(@Valid Chair chair)

    abstract List<Product> findAll()
   // abstract List<ShopTable> findAllTables()
   // abstract List<Chair> findAllChairs()
//

    abstract Number count()


    abstract Product find(@NotNull Long id)
    abstract ShopTable findTable(@NotNull Long id)
    abstract Chair findChair(@NotNull Long id)

}