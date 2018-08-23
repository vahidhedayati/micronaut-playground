package orders.controller

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import orders.service.OrderService
import orders.view.OrderView

@Controller("/")
class OrderController {

    OrderService orderService

    OrderController(OrderService orderService) {
        this.orderService = orderService
    }

    @Get("/")
    List<OrderView> findAll() {
        orderService.findAll().collect {
            orderService.toView(it)
        }
    }
    @Get("/custom")
    List doIt() {
        return orderService.customList()
    }


}