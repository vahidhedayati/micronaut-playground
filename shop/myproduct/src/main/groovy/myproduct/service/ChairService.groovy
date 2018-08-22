package myproduct.service

import grails.gorm.services.Service
import myproduct.domain.Chair

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(Chair)
interface ChairService {
    //Chair save(@Valid Chair chair)

    Chair save(@NotBlank String name, @NotBlank String title)
    List<Chair> findAll()
    int count()
    Chair find(@NotNull Long id)
}