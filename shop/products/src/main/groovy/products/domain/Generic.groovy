package products.domain

import grails.gorm.annotation.Entity


@Entity
class Generic extends Product {

    Integer width=0
    Integer height=0

    static mapping = {
        discriminator value: 'generic'
    }

}