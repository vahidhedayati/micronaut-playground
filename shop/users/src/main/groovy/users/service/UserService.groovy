package users.service

import grails.gorm.services.Service
import users.domain.User

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(User)
interface UserService {
    int count()
    User save(@NotBlank String username,@NotBlank String firstName,@NotBlank String lastName, @NotBlank String password)
    List<User> findAll()
    User find(@NotNull Long id)
}