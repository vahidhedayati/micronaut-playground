package products.init

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import products.domain.Chair
import products.domain.ShopTable
import products.service.ProductService

import javax.inject.Singleton

@Slf4j
@Singleton
@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final ProductService productService

    DataLoader(ProductService productService) {
        this.productService = productService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        if (!productService.count()) {
            log.debug "Loading sample data"

            ShopTable table = new ShopTable(name: 'Executive Chair', description: "Exclusive table available in our shop for a limited period, hand crafted.",width:1400,height:300,price:554.55 as BigDecimal, date: new Date())
            productService.save(table)

            ShopTable table1 = new ShopTable(name: 'Mid Executive Table', description: """
             Exclusive table available in our shop for a limited period, hand crafted.
            """,width:1200,height:300,price:314.55 as BigDecimal, date: new Date())
            productService.save(table1)

            ShopTable table2 = new ShopTable(name: 'Standard Table', description: """
             Table available in our shop for a limited period, hand crafted.
            """,width:500,height:300,price:154.55 as BigDecimal, date: new Date())
            productService.save(table2)


            Chair chair = new Chair(name: 'Executive Table', description: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:300,height:300,price:254.55 as BigDecimal, date: new Date(),wheels:true)
            productService.save(chair)

            Chair chair1 = new Chair(name: 'Mid Executive chair', description: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300,price:114.55 as BigDecimal, date: new Date())
            productService.save(chair1)

            Chair chair2 = new Chair(name: 'Standard chair', description: """
             Chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300,price:54.55 as BigDecimal, date: new Date())
            productService.save(chair2)




        }
    }
}
