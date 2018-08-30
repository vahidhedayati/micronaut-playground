package groovysocket.service

import grails.gorm.transactions.Transactional
import groovysocket.domain.User

class NonUserService {


    @Transactional(readOnly = true)
    User lookup(String username) {
        final String query = """
            from User where lower(username) =:username
            """
        return User.executeQuery(query,[username:username.toLowerCase()],[readOnly:true])[0]

    }
}