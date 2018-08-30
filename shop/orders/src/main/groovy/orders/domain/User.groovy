package orders.domain
import grails.gorm.annotation.Entity

@Entity
class User {

    String username
    String firstName
    String lastName

    String password

    static constraints = {
        password nullable: true
    }


    Map loadValues() {
        Map result=[:]
        result.username=username
        result.firstName=firstName
        result.lastName=lastName
        return result
    }

}