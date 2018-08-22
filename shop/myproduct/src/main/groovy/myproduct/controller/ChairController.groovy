package myproduct.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import myproduct.domain.Chair
import myproduct.service.ChairService

@Controller("/")


class ChairController {
    final ChairService chairService


    ChairController(ChairService chairService) {
        this.chairService = chairService
    }


    @Get("/")
    List<Chair> list() {
        return chairService.findAll()
    }

    @Get("/{id}")
    Chair show(Long id) {
        return chairService.find(id)
    }
}