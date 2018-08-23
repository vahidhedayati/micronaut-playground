package orders.init

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import orders.domain.Orders
import orders.service.OrderService

import javax.inject.Singleton

@Slf4j
@CompileStatic
@Singleton
@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final OrderService orderService

    DataLoader(OrderService orderService) {
        this.orderService = orderService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        if (!orderService.count()) {
            log.debug "Loading sample data"
            Orders order = new Orders(productId: 1, userId: 1, quantity:1, date: new Date(), price:55.50g)
            orderService.save(order)

            Orders order1 = new Orders(productId: 2, userId: 1, quantity:6, date: new Date(), price:155.50g)
            orderService.save(order1)

        }
    }
}
