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
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import micronaut.demo.beer.BeerSocketHandler;
import micronaut.demo.beer.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@CompileStatic
@Singleton
public class BootService implements ApplicationEventListener<ServerStartupEvent> {

    final EmbeddedServer embeddedServer;
    final BillService billService;

            ;
    public static final ConcurrentMap<String, BeerSocketHandler> liveConnections = new ConcurrentHashMap();

    final static Logger log = LoggerFactory.getLogger(BootService.class);
  //  public static ArrayList<String> availableSocketServers=new ArrayList<>();

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {

        connect();


     /*   Flowable
                .timer(1, SECONDS)
                .flatMapCompletable(i -> updateAvailableSocketServers())
                .repeat()
                .subscribe();
*/
        /**
         * This is a repeatable block that sends a ping to all connected liveConnections
         * concurrentMap - keeping channel open every 10 seconds
         */

        Flowable
                .timer(1, SECONDS)
                .flatMapCompletable(i -> pingAvailableSocketServers())
                .repeat()
                .subscribe();

    }

    /*
    Completable updateAvailableSocketServers() {
        RunFindServices r = new RunFindServices();
        return Completable
                .fromRunnable(r)
                .subscribeOn(Schedulers.io())
                .doOnError(e -> log.error("Stuff failed", e))
                .onErrorComplete();
    }
    */

    /**
     * This is the Completable object passed to Flowable aboe
     * @return
     */
    Completable pingAvailableSocketServers() {
        RunPing r = new RunPing(this);
        return Completable
                .fromRunnable(r)
                .subscribeOn(Schedulers.io())
                .doOnError(e -> log.error("Stuff failed", e))
                .onErrorComplete();
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
                    HealthService.Service service = healthService.getService();
                    String hostPort = service.getAddress() + ":" + service.getPort();
                    connect(hostPort);
                });
    }

    public void connect(String hostPort) {
        BeerSocketHandler cl = findSocketHandler(hostPort);
        final String url = "ws://"+hostPort+"/ws/"+embeddedServer.getPort();
        //This now connects back to all running instances of websocket server
        //Sends through the current port of this application
        try {
            WebSocketClient client;
            if (cl!=null) {
                client = cl.getClient();
            } else {
                // if (embeddedServer.getPort()!=service.getPort()) {}
                System.out.println("About to connect to websocket server for beersocket running on "+hostPort);
                client = new WebSocketClient(url,billService,this);
                client.open();
            }
            // Connection is kept open here
            //The connection is also added to local concurrent hashMap
            addSocket(hostPort,client);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException ignored) {
//intentionally ignored
        }
    }


    public static WebSocketClient findSocket(String hostPort) {
        return liveConnections.get(hostPort).getClient();
    }
    public static BeerSocketHandler findSocketHandler(String hostPort) {
        return liveConnections.get(hostPort);
    }
    public static void addSocket(String hostPort, WebSocketClient client) {
        liveConnections.putIfAbsent(hostPort,new BeerSocketHandler(client,System.currentTimeMillis()));
    }

    public static void removeSocket(String hostPort) {
        System.out.println("Removing websocket host "+hostPort);
        liveConnections.remove(hostPort);
    }

    public void updateConnection(String hostPort) {
        ///System.out.print("Updating connection 0------------------------------------------------"+hostPort);
        BeerSocketHandler bs = liveConnections.get(hostPort);
        if (bs!=null) {
            //System.out.print("Updating connection 0------------------------------------------------ removing adding again");
            liveConnections.remove(hostPort);
            liveConnections.putIfAbsent(hostPort,new BeerSocketHandler(bs.getClient(),System.currentTimeMillis()));
        } else {
            System.out.println("Error "+hostPort+" not found adding a new record");
            connect(hostPort);
        }
    }
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
        Iterator it = BootService.liveConnections.entrySet().iterator();
        ArrayList<String> currentBootService = new ArrayList<>();
        ConsulClient client1 = new ConsulClient("localhost");

        Response<List<HealthService>> healthyServices = client1.getHealthServices("beersocket", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service = healthService.getService();
                    String hostPort=service.getAddress() + ":" + service.getPort();
                        currentBootService.add(hostPort);

                });
        //System.out.println("" + currentBootService.size() + " vs " + BootService.availableSocketServers.size());
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currentKey = (String) pair.getKey();
            //System.out.println("Current key"+currentKey);
            try {
                if (currentBootService.contains(currentKey)) {
                    ///System.out.println("Sending ping to " + currentKey + " " + new Date());
                    BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
                    WebSocketClient client= cl.getClient();
                    Long lastRun = cl.getTimeStamp();
                    Long diff = System.currentTimeMillis()-lastRun;
                    //System.out.println("Difference "+diff);
                    if (System.currentTimeMillis()-lastRun<5000) {
                        //System.out.println("Pinged back ");
                        //client.eval("{ping:true}");
                        client.eval("__PING__");
                        //client.ping();
                        //currentBootService.remove(currentKey);
                    } else {
                        //System.out.println("Oh dear ");
                        //BootService.removeSocket(currentKey);
                        it.remove();
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
                    //BootService.removeSocket(currentKey);
                }

                // client.ping();
            } catch (Exception e) {
                //Exception so lets remove it from healthy list of sockets
                BootService.removeSocket(currentKey);
                //e.printStackTrace();
            }
            //System.out.println(pair.getKey() + " = " + pair.getValue());
            // it.remove(); // avoids a ConcurrentModificationException
        }

        if (currentBootService.size()>0){
            System.out.println("" + currentBootService.size() + " vs " );
            for (Object item : (ArrayList<String>) currentBootService) {
                BeerSocketHandler cl = BootService.findSocketHandler((String) item);
                if (cl==null) {
                    System.out.println("Connecting new host missing " + (String) item);
                    bootService.connect((String) item);
                }
            }
        }

    }
}

/*
class RunFindServices implements Runnable {
    @Override
    public void run() {
        ConsulClient client1 = new ConsulClient("localhost");
        Response<List<HealthService>> healthyServices = client1.getHealthServices("beersocket", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service = healthService.getService();
                    String hostPort=service.getAddress() + ":" + service.getPort();
                    if (!BootService.availableSocketServers.contains(hostPort)) {
                        BootService.availableSocketServers.add(hostPort);
                    }
                });
    }
}
*/
