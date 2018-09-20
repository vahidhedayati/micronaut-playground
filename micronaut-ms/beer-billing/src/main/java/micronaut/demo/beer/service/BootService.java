package micronaut.demo.beer.service;

import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import micronaut.demo.beer.WebSocketClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@CompileStatic
@Singleton
public class BootService implements ApplicationEventListener<ServerStartupEvent> {
    final EmbeddedServer embeddedServer;
    final BillService billService;
    WebSocketClient client;

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        connect();
    }

    @Inject
    public BootService(EmbeddedServer embeddedServer, BillService billService) {
        this.embeddedServer = embeddedServer;
        this.billService=billService;
        //this.client = client;
    }


    WebSocketClient connect() {
        final String url = "ws://localhost:8085/ws/"+embeddedServer.getPort()+"a";
        //final String url = "ws://localhost:9000";
        try {
            client = new WebSocketClient(url,billService);
            client.open();
            System.out.println("Client connected ---------------------------------------------------------"+url);
            return client;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendMessage(String message) {
        try {
            //System.out.println("Sending message "+message);
            client.<String>eval(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
