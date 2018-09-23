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
import io.reactivex.*;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.TestSubscriber;
import micronaut.demo.beer.WebSocketClient;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@CompileStatic
@Singleton
public class BootService implements ApplicationEventListener<ServerStartupEvent> {
    final EmbeddedServer embeddedServer;
    final BillService billService;

    public static final ConcurrentMap<String, WebSocketClient> liveConnections = new ConcurrentHashMap();

    final static Logger log = LoggerFactory.getLogger(BootService.class);

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {

        connect();

        /**
         * This is a repeatable block that sends a ping to all connected liveConnections
         * concurrentMap - keeping channel open every 10 seconds
         */

        Flowable
                .timer(10, SECONDS)
                .flatMapCompletable(i -> doStuffAsync())
                .repeat()
                .subscribe();

    }

    /**
     * This is the Completable object passed to Flowable aboe
     * @return
     */
    Completable doStuffAsync() {
        MyRunnableImplementation r = new MyRunnableImplementation();
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
                    HealthService.Service service =healthService.getService();

                    String hostPort=service.getAddress()+":"+service.getPort();

                    WebSocketClient client = findSocket(hostPort);
                    if (client==null) {
                        // if (embeddedServer.getPort()!=service.getPort()) {}
                        System.out.println("About to connect to websocket server for beersocket running on "+hostPort);

                        //This now connects back to all running instances of websocket server
                        //Sends through the current port of this application
                        final String url = "ws://"+hostPort+"/ws/"+embeddedServer.getPort();
                        try {

                            client = new WebSocketClient(url,billService);
                            client.open();
                            // Connection is kept open here

                            //The connection is also added to local concurrent hashMap
                            addSocket(hostPort,client);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /*
    static <T> Observable<T> delayed(T x) {
        return Observable.create(
                subscriber -> {
                   // while (!subscriber.isDisposed()) {
                       // sleep(15, SECONDS);
                      //  Runnable r = () -> {
                            //sleep(15, SECONDS);

                            Iterator it = liveConnections.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                String currentKey = (String) pair.getKey();
                                try {
                                    System.out.println("Sending ping to " + currentKey+" "+new Date());
                                    WebSocketClient client = (WebSocketClient) pair.getValue();
                                    client.eval("{ping:true}");
                                   // client.ping();
                                } catch (Exception e) {
                                    //Exception so lets remove it from healthy list of sockets
                                    removeSocket(currentKey);
                                    //e.printStackTrace();
                                }
                                //System.out.println(pair.getKey() + " = " + pair.getValue());
                                it.remove(); // avoids a ConcurrentModificationException
                            }
                        //};
                       // new Thread(r).start();
                   // }
                });
    }
    */

    static void sleep(int timeout, TimeUnit unit) {
        try {
            unit.sleep(timeout);
        } catch (InterruptedException ignored) {
//intentionally ignored
        }
    }


    public static WebSocketClient findSocket(String hostPort) {
        return liveConnections.get(hostPort);
    }

    public static void addSocket(String hostPort, WebSocketClient client) {
        liveConnections.putIfAbsent(hostPort,client);
    }

    public static void removeSocket(String hostPort) {
        System.out.println("Removing websocket host "+hostPort);
        liveConnections.remove(hostPort);
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
class MyRunnableImplementation implements Runnable {
    @Override
    public void run() {

        ArrayList<String> validHosts=new ArrayList<>();
        ConsulClient client1 = new ConsulClient("localhost");
        Response<List<HealthService>> healthyServices = client1.getHealthServices("beersocket", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                            HealthService.Service service = healthService.getService();
                            validHosts.add(service.getAddress()+":"+service.getPort());
                        });

        Iterator it = BootService.liveConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            String currentKey = (String) pair.getKey();
            try {
                if (validHosts.contains(currentKey)) {
                    System.out.println("Sending ping to " + currentKey+" "+new Date());
                    WebSocketClient client = (WebSocketClient) pair.getValue();
                    client.eval("{ping:true}");
                } else {
                    System.out.println("Appears host  has gone offline - removing  " + currentKey+" "+new Date());
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

    }

}