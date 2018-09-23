package beer.websocket;

import beer.websocket.service.BootService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.util.ArrayList;

@ServerWebSocket("/ws/{hostName}")
public class TransactionWebSocket  {

    @Inject
    BootService bootService;

    public static ArrayList<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {

        System.out.print("Socket connection from: "+hostName+"\n");
        sessions.add(session);

        //Share sessions triggers all running instances of this app to share session ids with each other
        bootService.shareSessions();


        return session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }

/*
    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {
        System.out.println("String message");
        //TODO - this is currently I think sending backto itself rather than broadcasting semi working
        // return session.broadcast(msg);
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
    */
    @OnMessage
    public Publisher<ArrayList<?>> onMessage(ArrayList<?> message, WebSocketSession session) {
        //System.out.println("ArrayList message"+message+" "+message.getClass());
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
    /*
    @OnMessage
    public Publisher<TextWebSocketFrame> onMessage(TextWebSocketFrame message, WebSocketSession session) {
        //TODO - this is currently I think sending backto itself rather than broadcasting semi working
        // return session.broadcast(msg);
        System.out.println("WebSocketFrame message");
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
    public Publisher<JSONPObject> onMessage(JSONPObject message, WebSocketSession session) {
        //TODO - this is currently I think sending backto itself rather than broadcasting semi working
        // return session.broadcast(msg);
        System.out.println("JSONPObject message");
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
    public Publisher<PingWebSocketFrame> onMessage(PingWebSocketFrame message, WebSocketSession session) {
        //TODO - this is currently I think sending backto itself rather than broadcasting semi working
        // return session.broadcast(msg);
        System.out.println("PingWebSocketFrame message");
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
    */
    /**
     *  Using consul we are going to use a controller client to trigger a message to be sent to all nodes
     *  in this case the clients now send via controller rather than socket which triggers a socket message to go to all
     *  connected
     * @param message
     */

    public static void myBroadCast(String message) {
        for (int i = 0; i < sessions.size(); i++) {
            System.out.println(sessions.get(i).getId()+"-- sending a message "+message);
            sessions.get(i).sendAsync(message);
        }
    }



    public static void addSession(WebSocketSession session) {
            if (!sessions.contains(session)) {
                System.out.println("Session "+session.getId()+" received from another beersocket server");
                sessions.add(session);
            }
    }


    @OnClose
    public Publisher<String> onClose(String username, WebSocketSession session) {
        String msg = "[" + username + "] Disconnected!";
        sessions.remove(session);
        return session.send(msg);
    }

}
