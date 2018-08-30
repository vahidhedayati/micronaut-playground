package groovysocket.domain
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
        result.username=this.username
        result.firstName=this.firstName
        result.lastName=this.lastName
        return result
    }


}