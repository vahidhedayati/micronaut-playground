package orders.service

import grails.gorm.services.Service
import io.reactivex.Flowable
import io.reactivex.Maybe
import orders.domain.Orders
import orders.products.Product
import orders.products.ProductClient
import orders.users.User
import orders.users.UserClient
import orders.view.OrderView

import javax.inject.Inject
import javax.validation.Valid

@Service(Orders)
abstract class OrderService {

    @Inject
    UserClient userClient
    @Inject
    ProductClient productClient

    abstract Orders save(@Valid Orders orders)
    abstract List<Orders> findAll()
    abstract Number count()

    /**
     * Noob learning curve - I had a lot of different methods and simply not understood what blockingGet was doing
     * @param order
     * @return
     */

    OrderView toView(Orders order) {

        //Generate new View object
        OrderView orderView = new OrderView()

        //set orders default
        orderView.date = order.date
        orderView.quantity = order.quantity

        //Connect and pick up user info and bind to above
        User actualUser = userClient.findUser(order.userId).blockingGet()
        orderView.username = actualUser.username
        orderView.firstName = actualUser.firstName
        orderView.lastName = actualUser.lastName

        //Connect and pick up product info
        Product actualProduct  = productClient.findProduct(order.productId).blockingGet()
        orderView.price = actualProduct.price
        orderView.productName = actualProduct.name
        orderView.productDescription = actualProduct.description

        //println "--orderView = ${orderView.price} vs ${orderView.lastName}"

        //Return filled in object binding ther order with products and users
        return orderView
    }
}
