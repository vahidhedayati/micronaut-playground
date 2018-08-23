package orders.users

import io.micronaut.http.annotation.Get
import io.reactivex.Maybe

interface UserApi {
    @Get("/{id}")
    Maybe<User> findUser(Long id)
}