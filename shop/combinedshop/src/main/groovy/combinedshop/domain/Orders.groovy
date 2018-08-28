package conmbinedshop.domain

import grails.gorm.annotation.Entity

@Entity
class Orders {


    Long productId
    Long userId


    //Price at the time of product being purchased - underlying product may change price tomorrow
    BigDecimal price

    Integer quantity

    Date date


}