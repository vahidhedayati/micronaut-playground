package myproduct.domain

import grails.gorm.annotation.Entity


@Entity
class Chair  {

    String name
    String title
    static constraints = {
        title nullable: true
    }
}