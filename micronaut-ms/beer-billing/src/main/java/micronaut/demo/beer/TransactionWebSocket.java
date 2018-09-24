package micronaut.demo.beer;

import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import micronaut.demo.beer.service.BillService;
import org.reactivestreams.Publisher;

import javax.inject.Inject;

@ServerWebSocket("/ws")
public class TransactionWebSocket {

    EmbeddedServer embeddedServer;
    BillService billService;

    @Inject
    public TransactionWebSocket(EmbeddedServer embeddedServer,BillService billService) {
        this.embeddedServer = embeddedServer;
        this.billService=billService;


    }

    @OnOpen
    public Publisher<String> onOpen( WebSocketSession session) {
        //System.out.println("Message "+session.getId());
        return null;
    }

    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {
        return  session.send(message);
    }

    @OnClose
    public Publisher<String> onClose(WebSocketSession session) {
        String msg = "Disconnected!";
        return session.send(msg);
    }

}
