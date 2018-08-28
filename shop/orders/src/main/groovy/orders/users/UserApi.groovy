package orders.users

import io.micronaut.http.annotation.Get
import io.reactivex.Maybe
import orders.domain.User

interface UserApi {
    
    @Get("/{id}")
    Maybe<User> findUser(Long id)

    @Get("/parseIds/{ids}")
    List<User> findUserBatch(List<Long> ids)
}
