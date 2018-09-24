package beer.websocket;

import beer.websocket.service.BootService;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.micronaut.http.MediaType;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.websocketx.*;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ServerWebSocket("/ws/{hostName}")
public class TransactionWebSocket  {

    @Inject
    BootService bootService;
    @Inject
    EmbeddedServer embeddedServer;


    /**
     * Concurrent Map storing the websocket session of remote billing app as the key
     * then the Long is the value which stores the last time the ping response time was received
     */
    public static final ConcurrentMap<String, BeerSocketHandler> liveConnections = new ConcurrentHashMap();

   // public static ArrayList<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {

        System.out.print("Socket connection from: "+hostName+"--------------------------------------------------------------\n");
        //sessions.add(session);
        //liveConnections.putIfAbsent(session,System.currentTimeMillis());
        liveConnections.putIfAbsent(hostName, new BeerSocketHandler(session,System.currentTimeMillis()));
        //Share sessions triggers all running instances of this app to share session ids with each other
        //bootService.shareSessions();


        return null; //session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }


    @OnMessage
    public Publisher<String> onMessage(String message, WebSocketSession session) {
        System.out.println("String message");
        // return session.broadcast(msg);
        Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //String currentKey = (String) pair.getKey();
            BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
            WebSocketSession sess = cl.getSession();
            if (sess.getId() != session.getId()) {
                //System.out.println(session.getId()+"------------------------------->>>");
                sess.sendAsync(message);
            }
        }
        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
        return  session.send(message);
    }


    @OnMessage
    public Publisher<WebSocketFrame> onMessage(WebSocketFrame message, WebSocketSession session) {
        // return session.broadcast(msg);
        System.out.println("WebSocketFrame message");
        Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //String currentKey = (String) pair.getKey();
            BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
            WebSocketSession sess = cl.getSession();
            if (sess.getId() != session.getId()) {
                //System.out.println(session.getId()+"------------------------------->>>");
                sess.sendAsync(message);
            }
        }
        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
        return  session.send(message);
    }

    public static ArrayList<WebSocketSession> getLiveSessions() {
        ArrayList<WebSocketSession> sessions=new ArrayList<>();

        Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //String currentKey = (String) pair.getKey();
            BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
            WebSocketSession sess = cl.getSession();
            sessions.add((WebSocketSession) cl.getSession());
        }
        return sessions;
    }

    @OnMessage
    public Publisher<String> onMessage(byte[] message, WebSocketSession session) {
        // return session.broadcast(msg);
        //
        String content = new String(message);

        /**
         * Ping received actually contains the remotehost:port as part the message
         * __PING__>RemoteHost:RemotPort
         *
         * The end part is parsed out and connection time stamp is updated in the updateconnection bit below
         */

        if (content.trim().startsWith("__PING__")) {
            if (content.contains(">")) {
                String[] parts = new String(content).split(">");
                if (parts != null && parts.length >= 1) {
                    String hostPort = parts[1];
                    if (hostPort != null) {
                        //System.out.println("byte[] message" + content + "|" + embeddedServer.getHost() + ":" + embeddedServer.getPort());
                        content = "__PING__>" + embeddedServer.getHost() + ":" + embeddedServer.getPort();
                        updateConnection(hostPort);
                    }
                }
            }
        } else {
            Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                //String currentKey = (String) pair.getKey();
                BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
                WebSocketSession sess = cl.getSession();
                if (sess.getId() != session.getId()) {
                    //System.out.println(session.getId()+"------------------------------->>>");
                    sess.sendAsync(content);
                }
            }
        }

        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
       // PingWebSocketFrame ping = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4}));
        //return  session.send(new PongWebSocketFrame(((PingWebSocketFrame) ping).content()));
        return session.send(content);
    }
    /*
    @OnMessage
    public Publisher<PingWebSocketFrame> onMessage(PingWebSocketFrame message, WebSocketSession session) {
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
        Iterator it = TransactionWebSocket.liveConnections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //String currentKey = (String) pair.getKey();
            BeerSocketHandler cl = (BeerSocketHandler) pair.getValue();
            WebSocketSession sess = cl.getSession();
                sess.sendAsync(message);
        }
    }


/*
    public static void addSession(WebSocketSession session) {
            if (!sessions.contains(session)) {
                System.out.println("Session "+session.getId()+" received from another beersocket server");
                sessions.add(session);
            }
    }

*/

    @OnClose
    public Publisher<String> onClose(WebSocketSession session) {
        //sessions.remove(session);
        String hostPort = getKeyFromValue(liveConnections,session);
        String msg = "Disconnecting TWS "+hostPort;
        removeSocket(hostPort);
        return session.send(msg);
    }

    public static String getKeyFromValue(Map<String,BeerSocketHandler> hm, WebSocketSession value) {
        for (String o : hm.keySet()) {
            if (hm.get(o).getSession().equals(value)) {
                return o;
            }
        }
        return null;
    }

    public static WebSocketSession findSocket(String hostPort) {
        return liveConnections.get(hostPort).getSession();
    }
    public static BeerSocketHandler findSocketHandler(String hostPort) {
        return liveConnections.get(hostPort);
    }
    public static void addSocket(String hostPort, WebSocketSession client) {
        liveConnections.putIfAbsent(hostPort,new BeerSocketHandler(client,System.currentTimeMillis()));
    }

    public static void removeSocket(String hostPort) {
        System.out.println("Removing websocket host "+hostPort);
        liveConnections.remove(hostPort);
    }

    public void updateConnection(String hostPort) {
        //System.out.print("Updating connection 0------------------------------------------------"+hostPort);
        BeerSocketHandler bs = liveConnections.get(hostPort);
        if (bs!=null) {
          //  System.out.print("Updating connection 0------------------------------------------------ removing adding again");
            liveConnections.remove(hostPort);
            liveConnections.putIfAbsent(hostPort,new BeerSocketHandler(bs.getSession(),System.currentTimeMillis()));
        } else {
            System.out.println("Error "+hostPort+" not found adding a new record");
            bootService.connect(hostPort);
        }
    }

}
