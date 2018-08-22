package myproduct.init

import groovy.util.logging.Slf4j
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import myproduct.service.ChairService

@Slf4j
//@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {



    final ChairService chairService

    DataLoader(ChairService chairService) {
        this.chairService = chairService
    }




    @Override
    void onApplicationEvent(ServerStartupEvent event) {


            chairService.save(name: 'Executive Chair', title: "AA")


            chairService.save(name: 'Executive Chair2', title: "bb")

        chairService.save(name: 'Executive Chair3', title: "CC")
    }
}
