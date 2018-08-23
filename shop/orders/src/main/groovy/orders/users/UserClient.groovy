package orders.users

import io.micronaut.http.client.Client

@Client("users")
interface UserClient extends UserApi {

}