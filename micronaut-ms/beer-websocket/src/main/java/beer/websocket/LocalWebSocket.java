package beer.websocket;

import beer.websocket.service.BootService;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.util.ArrayList;

@ServerWebSocket("/ls/{hostName}")
public class LocalWebSocket {

    @Inject
    BootService bootService;

    public static ArrayList<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {
        if (!hostName.startsWith("SEND_")) {
            sessions.add(session);
        }


        //Share sessions triggers all running instances of this app to share session ids with each other
       // bootService.shareSessions();
        //myBroadCast("BROADCAST_SESSIONS");


        return session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }

    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {

        for (int i = 0; i < sessions.size(); i++) {
            WebSocketSession sess = sessions.get(i);
            if (sess.getId()!=session.getId()) {
                //System.out.println(session.getId()+"------------------------------->>>");
                sess.sendAsync(message);
            }
        }
        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
        return  session.send(message);
    }
    @OnMessage
    public Publisher<ArrayList<?>> onMessage(ArrayList<?> message, WebSocketSession session) {
        System.out.println("WE HAVE ARRAY");
        for (int i = 0; i < sessions.size(); i++) {
            WebSocketSession sess = sessions.get(i);
            if (sess.getId()!=session.getId()) {
                //System.out.println(session.getId()+"------------------------------->>>");
                sess.sendAsync(message);
            }
        }
        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
        return  session.send(message);
    }

    /**
     *  Using consul we are going to use a controller client to trigger a message to be sent to all nodes
     *  in this case the clients now send via controller rather than socket which triggers a socket message to go to all
     *  connected
     * @param message
     */


    public static void myBroadCast(String message) {
        for (int i = 0; i < sessions.size(); i++) {
            sessions.get(i).sendAsync( new TextWebSocketFrame(message));
        }
    }



    public static void addSession(WebSocketSession session) {
            if (!sessions.contains(session)) {
                System.out.println("Session "+session.getId()+" received from another beersocket server");
                sessions.add(session);
            }
    }


    @OnClose
    public Publisher<String> onClose(WebSocketSession session) {
        String msg = "[] Disconnected!";
        sessions.remove(session);
        return session.send(msg);
    }

}
