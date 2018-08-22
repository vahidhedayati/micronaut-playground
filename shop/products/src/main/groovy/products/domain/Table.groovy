package products.domain

import grails.gorm.annotation.Entity


@Entity
class Table extends Generic {

    static mapping = {
        discriminator value: 'table'
    }

}