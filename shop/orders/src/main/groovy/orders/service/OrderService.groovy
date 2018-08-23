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


    OrderView toView3(Orders order) {
        //Maybe.zip(userClient.findUser(order.userId), productClient.findProduct(order.productId)) { User actualUser, Product actualProduct ->
        Maybe(productClient.findProduct(order.productId)) { Product actualProduct ->
        return new OrderView(
                date: order.date,
                //  username: actualUser.username,
                // firstName: actualUser.firstName,
                //lastName: actualUser.lastName,
                price: actualProduct.price,
                productName: actualProduct.name,
                productDescription: actualProduct.description,
                quantity: order.quantity,
        )
        } as Maybe<OrderView>
    }

    Maybe<OrderView> toView34(Orders order) {
        println "-- UID: ${order.userId} vs ${order.productId} PID"
        Maybe<User> actualUser = userClient.findUser(order.userId)
        Maybe<Product> actualProduct = productClient.findProduct(order.productId)
        //Observable.from(actualUser).subscribe { println "Hello ${it}!" }
        (actualUser).subscribe { println "Hello ${it}!" }
        //actualUser?.each {
        //    println "---->>>>>>>>>>> ${Flowable.fromArray(it).subscribe()}"
        //}
        //actualUser?.flattenAsObservable().withLatestFrom().subscribe {println "${it}"}
        println "---- ${actualUser.toObservable().subscribe()} >>>>-------------"
            println "--- WE HAVE 0----------------------- ${actualUser.getClass()}  ${Flowable.fromArray(actualUser).subscribe()} vs ${actualProduct?.getClass()}  ${actualProduct} ------------------------------------------"
/*
            return new OrderView(
                    date: order.date,
                    username: actualUser.username,
                    firstName: actualUser.firstName,
                    lastName: actualUser.lastName,
                    price: actualProduct.price,
                    productName: actualProduct.name,
                    productDescription: actualProduct.description,
                    quantity: order.quantity,
            ) as Maybe<OrderView>
  */
        //} as Maybe<OrderView>
    }

    OrderView toViewOther(Orders order) {
        OrderView orderView = new OrderView()
        orderView.date = order.date
        orderView.quantity = order.quantity

        userClient.findUser(order.userId).toObservable().subscribe() { User actualUser ->
            orderView.username = actualUser.username
            orderView.firstName = actualUser.firstName
            orderView.lastName = actualUser.lastName


        }

        productClient.findProduct(order.productId).toObservable().subscribe() { Product actualProduct ->
            orderView.price = actualProduct.price
            orderView.productName = actualProduct.name
            orderView.productDescription = actualProduct.description
        }
        println "--orderView = ${orderView.price} vs ${orderView.lastName}"
        return orderView
    }


    Maybe<OrderView> toViewOld(Orders order) {
        println "-- UID: ${order.userId} vs ${order.productId} PID"

        //, productClient.findProduct(order.productId)
        //, Product actualProduct
        def aa =  productClient.findProduct(order.productId).toObservable()
        println "aa======================--- ${aa}"
        userClient.findUser(order.userId).toObservable().subscribe() { User actualUser->
            println "--- WE HAVE 0----------------------- ${actualUser.username}"// vs ${actualProduct?.getClass()} ------------------------------------------"

            return new OrderView(
                    date: order.date,
                    username: actualUser.username,
                    firstName: actualUser.firstName,
                    lastName: actualUser.lastName,
                    //price: actualProduct.price,
                    //productName: actualProduct.name,
                    //productDescription: actualProduct.description,
                    quantity: order.quantity,
            )
        } as Maybe<OrderView>
    }

    /**
     * Lack of knowledge around RXJAVA - learning curve
     * @param order
     * @return
     */

    def toView(Orders order) {
        println "-- UID: ${order.userId} vs ${order.productId} PID"

        //List<OrderView> orderView = []

        OrderView orderView= userClient.findUser(order.userId).subscribe() {User actualUser->
            println "--- WE HAVE 0----------------------- ${actualUser.username}"
// vs ${actualProduct?.getClass()} ------------------------------------------"

             return new OrderView(
                    productId: order.productId,
                     userId: order.userId,
                    date: order.date,
                    username: actualUser.username,
                    firstName: actualUser.firstName,
                    lastName: actualUser.lastName,
                    //price: actualProduct.price,
                    //productName: actualProduct.name,
                    //productDescription: actualProduct.description,
                    quantity: order.quantity,

            )

        }
        productClient.findProduct(order.productId).subscribe() { Product actualProduct ->
            Long pid = order.productId
          //  def ov = orderView.find{it.productId==pid}
           // if (ov) {
            orderView.price= actualProduct.price
            orderView.productName= actualProduct.name
            orderView.productDescription= actualProduct.description
            //}
        }
        println "---------------------------------------------------------- ORDER VIEW = ${orderView.size()}"
        orderView?.each {
            println "----- > ${it.username} ${it.price} >>>>>>>>"
        }


        return orderView
    }
}
