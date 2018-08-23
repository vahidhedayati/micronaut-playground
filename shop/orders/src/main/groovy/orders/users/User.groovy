package orders.users
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

}