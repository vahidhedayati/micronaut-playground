package orders.service

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import io.reactivex.Flowable
import io.reactivex.Maybe
import orders.domain.Orders
import orders.products.Product
import orders.products.ProductClient
import orders.domain.User
import orders.users.UserClient
import orders.view.OrderView
import org.hibernate.transform.AliasToEntityMapResultTransformer

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

    /**
     * This is a working example querying the existing domain class using HQL query instead this works :
     * http://localhost:8180/custom
     * produces: [{"date":1535038163686,"productId":1,"orderId":1,"userId":1,"price":55.50},{"date":1535038163743,"productId":2,"orderId":2,"userId":1,"price":155.50}]
     *
     *
     * Was unable to cross connect to Product or User since they are on the other microservice apps  -
     * there is nothing stopping using something like above to pass bits of below or all of below to remote calls
     * that then do HQL queries to return results this way if you prefer
     * @param startOrganisationId
     * @return
     */
    @Transactional(readOnly = true)
    List customList() {

        final String query = """select new map( o.id as orderId, o.userId as userId,
                                                o.productId as productId,o.date as date,o.price as price

                                               )
            from Orders o  order by o.id

"""

        /*
        * This was an attempt to cross join with other microservices which failed

                                                u.username as username,
                                                p.name as productName,
                                                p.description as productDescription


        , User u, Product p


                                                where p.id=o.productId and u.id=o.userId

         */


        //But.....
        //We now have an overall map of all of above
        def results = Orders.executeQuery(query,[],[readOnly:true])
        def userResults=[]
        def productResult=[]

        //Set it to be blank if there is results otherwise return results this will be final output
        List finalResults=results ? [] : results
        if (results) {
            userResults = userClient.findUserBatch(results.userId)
            productResult = productClient.findProductBatch(results.productId)
            results?.each { res->
                finalResults << (res.collect{it}+productResult?.find{it.id==res.productId}?.collect{it}+userResults?.find{it.id==res.userId}?.collect{it})
            }
        }
        return finalResults
    }
}
