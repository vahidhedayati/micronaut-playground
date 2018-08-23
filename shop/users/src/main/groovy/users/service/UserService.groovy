package users.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import users.domain.User

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Service(User)
abstract class UserService {
    abstract User save(@Valid User user)
    abstract List<User> findAll()
    abstract Number count()
    abstract User save(@NotBlank String username,@NotBlank String firstName,@NotBlank String lastName, @NotBlank String password)
    abstract User find(@NotNull Long id)

    @Transactional(readOnly = true)
    List findBatch(List<Long> ids) {
        //select new map( id as userId, username as username,
        //firstName as firstName, lastName as lastName)
        final String query = """
            from User where id in (:ids)
            """
        return User.executeQuery(query,[ids:ids],[readOnly:true])
    }


}