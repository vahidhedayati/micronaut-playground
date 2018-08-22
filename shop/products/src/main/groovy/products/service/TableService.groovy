package products.service

import grails.gorm.services.Service
import products.domain.Chair
import products.domain.Product
import products.domain.Table

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service(Table)
interface TableService {
    Table save(@Valid Table Table)
    List<Table> findAll()
    Number count()
    Table findTable(@NotNull Long id)
}