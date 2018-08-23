package products.service

import grails.gorm.services.Service
import products.domain.ShopTable

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service(ShopTable)
interface ShopTableService {
    ShopTable save(@Valid ShopTable Table)
    List<ShopTable> findAll()
    int count()
    ShopTable find(@NotNull Long id)
}