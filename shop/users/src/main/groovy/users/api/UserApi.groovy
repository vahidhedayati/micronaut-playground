package users.api

import io.micronaut.http.annotation.Get
import users.domain.User


interface UserApi {

    @Get("/")
    List<User> listUsers()

    @Get("/{id}")
    User show(Long id)

}