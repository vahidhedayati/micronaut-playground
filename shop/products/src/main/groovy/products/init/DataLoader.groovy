package products.init

import groovy.util.logging.Slf4j
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import products.domain.Chair
import products.domain.ShopTable
import products.service.ChairService
import products.service.ProductNonExtendedService
import products.service.ProductService
import products.service.ShopTableService

@Slf4j
//@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final ProductService productService
    final ProductNonExtendedService productNonExtendedService

    final ChairService chairService
    final ShopTableService shopTableService

    DataLoader(ProductNonExtendedService productNonExtendedService) {
        this.productNonExtendedService = productNonExtendedService
    }

    DataLoader(ProductService productService) {
        this.productService = productService
    }

    DataLoader(ChairService chairService) {
        this.chairService = chairService
    }
    DataLoader(ShopTableService shopTableService) {
        this.shopTableService = shopTableService
    }



    @Override
    void onApplicationEvent(ServerStartupEvent event) {

       // if (!productService.count()) {
            log.debug "Loading sample data----------------------------------------------------------------------"
        log.error "Loading sample data -------------------------------------------------------------"
        println "Loading sample data -------------------------------------------------------------"

           productNonExtendedService.save(name: 'Test product', title: "Test available")

            productNonExtendedService.save(name: 'Test product2', title: "Test available2")

           // Product product = new Product(name: 'Test product', description: "Test available")//,price:554.55g, date: new Date())
           productService.save(name: 'Test product', title: "Test available")


            ShopTable table = new ShopTable(name: 'Executive Chair', title: "Exclusive table available in our shop for a limited period, hand crafted.",width:1400,height:300)//,price:554.55g, date: new Date())
        shopTableService.save(table)

            println "-- ${table} added"
        ShopTable table1 = new ShopTable(name: 'Mid Executive Table', title: """
             Exclusive table available in our shop for a limited period, hand crafted.
            """,width:1200,height:300)//,price:314.55g, date: new Date())
        shopTableService.save(table1)

        ShopTable table2 = new ShopTable(name: 'Standard Table', title: """
             Table available in our shop for a limited period, hand crafted.
            """,width:500,height:300)//,price:154.55g, date: new Date())
        shopTableService.save(table2)


            Chair chair = new Chair(name: 'Executive Chair', title: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:300,height:300)//,price:254.55g, date: new Date(),wheels:true)
            chairService.save(chair)

            Chair chair1 = new Chair(name: 'Mid Executive Chair', title: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300)//,price:114.55g, date: new Date())
            chairService.save(chair1)

            Chair chair2 = new Chair(name: 'Standard Chair', title: """
             Chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300)//,price:54.55g, date: new Date())
            chairService.save(chair2)


       // }
    }
}
