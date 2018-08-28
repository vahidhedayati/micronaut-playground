package combinedshop

import io.micronaut.runtime.Micronaut
import groovy.transform.CompileStatic

@CompileStatic
class Application {
    Closure doWithSpring() {
        {->
            wsChatConfig DefaultWsChatConfig
        }
    }
    static void main(String[] args) {
        Micronaut.run(Application)
    }
}