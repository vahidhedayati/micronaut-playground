package myproduct.init

import groovy.util.logging.Slf4j
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import myproduct.service.ChairService
import javax.inject.Singleton

@Slf4j
//@Requires(notEnv = Environment.BARE_METAL)
@Singleton
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {



    final ChairService chairService

    DataLoader(ChairService chairService) {
        this.chairService = chairService
    }




    @Override
    void onApplicationEvent(ServerStartupEvent event) {


            chairService.save('Executive Chair',  "AA")


            chairService.save('Executive Chair2',  "bb")

        chairService.save('Executive Chair3', "CC")
    }
}
