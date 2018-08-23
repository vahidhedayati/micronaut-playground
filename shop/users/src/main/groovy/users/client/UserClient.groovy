package users.client

import io.micronaut.http.client.Client
import users.api.UserApi

@Client("/")
interface UserClient extends UserApi {}