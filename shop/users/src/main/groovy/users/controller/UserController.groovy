package users.controller

import groovy.transform.CompileStatic
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import users.domain.User
import users.service.UserService

@Controller("/")
@CompileStatic
class UserController {
    final UserService userService

    UserController(UserService userService) {
        this.userService = userService
    }

    @Get("/")
    List<User> listUsers() {
        return userService.findAll()
    }

    @Get("/{id}")
    User show(Long id) {
        return userService.find(id)
    }
}