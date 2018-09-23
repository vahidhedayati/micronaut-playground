package micronaut.demo.beer;

import io.micronaut.http.MediaType;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
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
     * @param hostName This is sent by remote socket application sends it's own hostname:port to this server
     *                 this server simply connects to this host:port and sends it's own port
     *
     * @param session
     * @return
     */
    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {

        //Check to see if there is already a connection added to the concurrent map
        //Should not be there but just incase
        WebSocketClient client = BootService.findSocket(hostName);
        if (client==null) {

            System.out.print("Socket connection from: "+hostName+"\n");
            final String url = "ws://"+hostName+"/ws/"+embeddedServer.getPort();
            //final String url = "ws://localhost:9000";
            try {
                client = new WebSocketClient(url,billService);
                client.open();
                //Keep connection open and add it to the existing connCurrent Maps
                BootService.addSocket(hostName,client);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }

    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {
        return  session.send(message);
    }


    @OnMessage
    public Publisher<PongWebSocketFrame> onMessage(PongWebSocketFrame message, WebSocketSession session) {
        System.out.println("PongWebSocketFrame message");
        return  session.send(message);
    }

    @OnClose
    public Publisher<String> onClose(WebSocketSession session) {
        String msg = "Disconnected!";
        return session.send(msg);
    }

}
