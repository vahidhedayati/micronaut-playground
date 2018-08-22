package products.service

import grails.gorm.services.Service
import products.domain.Chair

import javax.validation.Valid
import javax.validation.constraints.NotNull

@Service(Chair)
interface ChairService {
    Chair save(@Valid Chair chair)
    List<Chair> findAll()
    Number count()
    Chair find(@NotNull Long id)
}