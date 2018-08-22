package products.service

import grails.gorm.services.Service
import products.domain.ProductNonExtended

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(ProductNonExtended)
interface ProductNonExtendedService {
    ProductNonExtended save(@NotBlank String name, @NotBlank String title)
    List<ProductNonExtended> findAll()
    int count()
    ProductNonExtended find(@NotNull Long id)

}