package beer.websocket.service;

import beer.websocket.BeerSocketHandler;
import beer.websocket.TransactionWebSocket;
import beer.websocket.WebSocketClient;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import groovy.transform.CompileStatic;
import groovy.util.logging.Slf4j;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.websocket.WebSocketSession;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@CompileStatic
@Singleton
public class BootService   implements ApplicationEventListener<ServerStartupEvent> {
    final EmbeddedServer embeddedServer;
    final static Logger log = LoggerFactory.getLogger(BootService.class);
    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        connect();
        Flowable
                .timer(1, SECONDS)
                .flatMapCompletable(i -> pingAvailableBillingServers())
                .repeat()
                .subscribe();
    }
    /**
     * This is the Completable object passed to Flowable above
     * @return
     */
    Completable pingAvailableBillingServers() {
        RunPing r = new RunPing(this);
        return Completable
                .fromRunnable(r)
                .subscribeOn(Schedulers.io())
                .doOnError(e -> log.error("Stuff failed", e))
                .onErrorComplete();
    }
    @Inject
    public BootService(EmbeddedServer embeddedServer) {
        this.embeddedServer = embeddedServer;
    }

    void connect() {
        /**
         * This at the moment is using a single consul host running locally
         * in wider world would point to a consul cluster
         * // compile "com.ecwid.consul:consul-api:1.4.1"
         *
         * sorry unsure perhaps micronaut has better ways seen classes covering consul discovery - wasn't sure how
         */

        //ConsulClient client1 = new ConsulClient("localhost");

        /**
         * We ask consulClient to list all the healthynodes of beersocket applications running;
         */
        /*
        Response<List<HealthService>> healthyServices = client1.getHealthServices("billing", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service =healthService.getService();
                    // if (embeddedServer.getPort()!=service.getPort()) {}
                    System.out.println("About to connect to billing server for beer-websocket running on "+service.getAddress()+"/"+ service.getPort());

                    //This now connects back to all running instances of websocket server
                    //Sends through the current port of this application as well as its name making up most of the ws://{host}:{port} up
                    //otherside simply triggers socket connection to it


                    connect(service.getAddress()+":"+service.getPort());

                });
        */
    }

    public void connect(String hostName) {
        final String url = "ws://"+hostName+"/ws/"+embeddedServer.getHost()+":"+embeddedServer.getPort();
        try {
            //All we do is connect - nothing else
            WebSocketClient client = new WebSocketClient(url);
            client.open();
            client.close();
            //System.out.println("Client connected ---------------------------------------------------------"+url+"\n");
            /**
             *
             * The connection will trigger those apps to locally now connect with this newly started socked app
             * // so in effect add them to their socket pool of connected socket servers
             *
             * This is really now an overkill I think the main app connecting to one socket server that then shares sessions each time any get a new
             * connection from billing is sufficient - but assuming we have 3 socket server 4th one is added and first 3 are killed i think it
             * should still work because the actual message sent via the billing app to socket app is using consul and routing from a client http
             * connector which then sends to all connected sockets
             *
             * the connected sockets is a maze that all the billing applications maintain
             */


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public  void shareSessions() {
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
                    if (embeddedServer.getPort()!=service.getPort()) {
                        System.out.println("About to connect to websocket server for beersocket running on " + service.getAddress() + "/" + service.getPort());
                        //This now connects back to all running instances of websocket server
                        //Sends through the current port of this application
                        final String url = "ws://" + service.getAddress() + ":" + service.getPort() + "/ls/" + embeddedServer.getPort();
                        //final String url = "ws://localhost:9000";
                        try {
                            WebSocketClient client = new WebSocketClient(url);
                            client.open();
                            client.sendSessions(TransactionWebSocket.getLiveSessions());
                            client.close();
                            //System.out.println("Client connected ---------------------------------------------------------" + url + "\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
/**
 * This Should be in its own class - is used by the Completable above that is passed in to the Flowable
 * at the very top
 * @return
 */
class RunPing implements Runnable {
    BootService bootService;
    @Inject
    RunPing(BootService bootService) {
        this.bootService=bootService;
    }
    @Override
    public void run() {
        Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
        ArrayList<String> currentBootService = new ArrayList<>();
        ConsulClient client1 = new ConsulClient("localhost");

        Response<List<HealthService>> healthyServices = client1.getHealthServices("billing", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service = healthService.getService();
                    String hostPort=service.getAddress() + ":" + service.getPort();
                    currentBootService.add(hostPort);

                });
         boolean resendResults=false;
        boolean resendResults1=false;
        boolean resendResults2=false;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currentKey = (String) pair.getKey();
            //System.out.println("Current key ----------------------------------------------------------------------"+currentKey+" "+currentBootService);
            //System.out.println(currentBootService.size()+" size "+currentBootService+" vs "+currentKey);

            try {
                if (currentBootService.contains(currentKey)) {

                    //System.out.println("Sending ping to " + currentKey + " " + new Date());
                    BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
                    WebSocketSession client= cl.getSession();
                    Long lastRun = cl.getTimeStamp();
                    Long diff = System.currentTimeMillis()-lastRun;
                    //System.out.println("Difference "+diff);
                    if (diff>5000) {
                        System.out.println("DIFFF "+diff+" ------------------------ removing");
                        it.remove();
                        System.out.println("RESENd 1 -----------------------------------------------");
                        resendResults=true;
                    }
                    Iterator<String> ita = currentBootService.iterator();
                    while (ita.hasNext()) {
                        String c = ita.next();
                        if (c.equals(currentKey)) {
                            ita.remove();
                        }
                    }
                } else {
                    System.out.println("Appears host  has gone offline - removing  " + currentKey + " " + new Date());
                    it.remove();
                    System.out.println("RESENd 2 -----------------------------------------------");
                    resendResults2=true;
                    //BootService.removeSocket(currentKey);
                }
                // client.ping();
            } catch (Exception e) {
                //Exception so lets remove it from healthy list of sockets
                TransactionWebSocket.removeSocket(currentKey);
                e.printStackTrace();
            }
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            // it.remove(); // avoids a ConcurrentModificationException
        }

        if (currentBootService.size()>0){
            System.out.println("" + currentBootService.size() + " vs " );
            for (Object item : (ArrayList<String>) currentBootService) {
                BeerSocketHandler cl = TransactionWebSocket.findSocketHandler((String) item);
                if (cl==null) {
                    //if (TransactionWebSocket.liveConnections.size()>0){
                        System.out.println("RESENd 3 -----------------------------------------------");
                        resendResults=true;
                    //}
                    System.out.println("Connecting new host missing " + (String) item);
                    bootService.connect((String) item);
                }
            }
        }
        if (resendResults==true) {
            System.out.println("Sharing results");
            bootService.shareSessions();
        }

    }
}