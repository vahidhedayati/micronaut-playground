package micronaut.demo.beer.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import micronaut.demo.beer.WebSocketClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@CompileStatic
@Singleton
public class BootService implements ApplicationEventListener<ServerStartupEvent> {
    final EmbeddedServer embeddedServer;
    final BillService billService;

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


    void connect() {
        /**
         * This at the moment is using a single consul host running locally
         * in wider world would point to a consul cluster
         * // compile "com.ecwid.consul:consul-api:1.4.1"
         *
         * sorry unsure perhaps micronaut has better ways seen classes covering consul discovery - wasn't sure how
         */
        ConsulClient client1 = new ConsulClient("localhost");

        /**
         * We ask consulClient to list all the healthynodes of beersocket applications running;
         */
        Response<List<HealthService>> healthyServices = client1.getHealthServices("beersocket", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service =healthService.getService();
                   // if (embeddedServer.getPort()!=service.getPort()) {}
                    System.out.println("About to connect to websocket server for beersocket running on "+service.getAddress()+"/"+ service.getPort());

                    //This now connects back to all running instances of websocket server
                    //Sends through the current port of this application

                    final String url = "ws://"+service.getAddress()+":"+service.getPort()+"/ws/"+embeddedServer.getPort();
                    //final String url = "ws://localhost:9000";
                    try {
                        WebSocketClient client = new WebSocketClient(url,billService);
                        client.open();
                        System.out.println("Client connected ---------------------------------------------------------"+url+"\n");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                });
    }


    /**
     * We have got rid of unsafe thread aspect of client and no longer use SendMessage
     * instead we use consul http to send to 1 of the websocket servers which in turn relay out to all
     * connected nodes which ever socket server is selected
     */
    /*
    public void sendMessage(String message) {
        try {
            //System.out.println("Sending message "+message);
            client.<String>eval(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */


}
