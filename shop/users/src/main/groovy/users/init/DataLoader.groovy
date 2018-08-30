package users.init

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import users.service.UserService

import javax.inject.Singleton

@Slf4j
@CompileStatic
@Singleton
@Requires(notEnv = Environment.BARE_METAL)
class DataLoader implements ApplicationEventListener<ServerStartupEvent> {

    final UserService userService
    private static final int PORT = 9000;

    DataLoader(UserService userService) {
        this.userService = userService
    }

    @Override
    void onApplicationEvent(ServerStartupEvent event) {
        if (!userService.count()) {
            log.debug "Loading sample data"
            userService.save("jsmith", "John", "Smith", "password")
            userService.save("cjones", "Casey", "Jones", "password1")
            userService.save("mbutler", "Mike", "Butler", "password2")
            userService.save("jhanes", "Jim", "Hanes", "password3")
            userService.save("ksmith", "Kevin", "Smith", "password4")
        }

    }
}
