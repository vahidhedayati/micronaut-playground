package products.init

import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import products.domain.Chair
import products.domain.Product
import products.domain.Table
import products.service.ChairService
import products.service.ProductService
import products.service.TableService

@Slf4j
@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final ProductService productService


    final ChairService chairService
    final TableService tableService

    DataLoader(ProductService productService) {
        this.productService = productService
    }

    DataLoader(ChairService chairService) {
        this.chairService = chairService
    }
    DataLoader(TableService tableService) {
        this.tableService = tableService
    }
    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        if (!productService.count()) {
            log.info "Loading sample d.ata"


           // Product product = new Product(name: 'Test product', description: "Test available")//,price:554.55g, date: new Date())
           productService.save(name: 'Test product', description: "Test available")

            Table table = new Table(name: 'Executive Chair', description: "Exclusive table available in our shop for a limited period, hand crafted.",width:1400,height:300)//,price:554.55g, date: new Date())
            tableService.save(table)

            println "-- ${table} added"
            Table table1 = new Table(name: 'Mid Executive Table', description: """
             Exclusive table available in our shop for a limited period, hand crafted.
            """,width:1200,height:300)//,price:314.55g, date: new Date())
            tableService.save(table1)

            Table table2 = new Table(name: 'Standard Table', description: """
             Table available in our shop for a limited period, hand crafted.
            """,width:500,height:300)//,price:154.55g, date: new Date())
            tableService.save(table2)


            Chair chair = new Chair(name: 'Executive Chair', description: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:300,height:300)//,price:254.55g, date: new Date(),wheels:true)
            chairService.save(chair)

            Chair chair1 = new Chair(name: 'Mid Executive Chair', description: """
             Exclusive chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300)//,price:114.55g, date: new Date())
            chairService.save(chair1)

            Chair chair2 = new Chair(name: 'Standard Chair', description: """
             Chair available in our shop for a limited period, hand crafted.
            """,width:200,height:300)//,price:54.55g, date: new Date())
            chairService.save(chair2)




        }
    }
}
