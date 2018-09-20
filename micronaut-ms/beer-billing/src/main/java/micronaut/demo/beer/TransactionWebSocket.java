package micronaut.demo.beer;

import io.micronaut.http.MediaType;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import micronaut.demo.beer.service.BillService;
import micronaut.demo.beer.service.BootService;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

@ServerWebSocket("/ws/{hostName}")
public class TransactionWebSocket {

    EmbeddedServer embeddedServer;
    BillService billService;

    @Inject
    public TransactionWebSocket(EmbeddedServer embeddedServer,BillService billService) {
        this.embeddedServer = embeddedServer;
        this.billService=billService;
    }

    /**
     * This happens when a new billing socket application is fired up after these apps have been fired up
     * @param hostName
     * @param session
     * @return
     */
    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {
        System.out.print("Socket connection from: "+hostName+"\n");

        final String url = "ws://"+hostName+"/ws/"+embeddedServer.getPort();
        //final String url = "ws://localhost:9000";
        try {
            WebSocketClient client = new WebSocketClient(url,billService);
            client.open();
            System.out.println("Client connected ---------------------------------------------------------"+url+"\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }

    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {
        return  session.send(message);
    }


    @OnClose
    public Publisher<String> onClose(String username, WebSocketSession session) {
        String msg = "[" + username + "] Disconnected!";
        return session.send(msg);
    }

}
