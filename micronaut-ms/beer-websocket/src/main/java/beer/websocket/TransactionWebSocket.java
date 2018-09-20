package beer.websocket;

import io.micronaut.http.MediaType;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import org.reactivestreams.Publisher;

import java.util.ArrayList;

@ServerWebSocket("/ws/{hostName}")
public class TransactionWebSocket  {

    ///ArrayList<E> obj = new ArrayList<E>();
    private static ArrayList<WebSocketSession> sessions = new ArrayList<WebSocketSession>();

    //String ticket,
    @OnOpen
    public Publisher<String> onOpen(String hostName, WebSocketSession session) {
        String msg = "" + hostName; ///+ ":"+beerName+':'+size;
        //BeerItem.Size.valueOf(size)
        System.out.print("________________________"+msg);
        sessions.add(session);
        return session.send("{ hostName:"+hostName+"}", MediaType.APPLICATION_JSON_TYPE);
    }

    @OnMessage
    public Publisher<String> onMessage(
            String message,
            WebSocketSession session
    ) {
       //JsonParser jsonParser = new JsonFactory().createJsonParser(username);

/*

        String msg = "[" + message+ "] ";//+beerName;// + beerName+' '+size;
        System.out.println("\n"+message+"\n :::::1");

        //TODO - this is currently I think sending backto itself rather than broadcasting semi working
       // return session.broadcast(msg);
        Object mssg=message;
        try {
            session.sendAsync(message,  MediaType.APPLICATION_JSON_TYPE).get();
        } catch (Exception e) {
            System.out.println("errror :::::1");
            e.printStackTrace();
        }
*/

            for (int i = 0; i < sessions.size(); i++) {
                WebSocketSession sess = sessions.get(i);
                if (sess.getId()!=session.getId()) {
                    //System.out.println(session.getId()+"------------------------------->>>");
                    sess.sendAsync(message);
                }

            }
            /*
        try {
            sessions.forEach(c ->

                    c.send(message)
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        */

        //return  session.broadcast(msg, MediaType.APPLICATION_JSON_TYPE);
        return  session.send(message);
    }

    /*
    @OnMessage
    public Publisher<String> onMessage(
            String username,
             WebSocketSession session
    ) {
        String msg = "[" + username + "] ";//+beerName;// + beerName+' '+size;
        System.out.println(msg+"2");
        return session.send(msg);
        //return null;
    }

    @OnMessage
    public Publisher<String> onMessage(
            String username,
            String beerName, String size, WebSocketSession session
            ) {
        String msg = "[" + username + "] ";//+beerName;// + beerName+' '+size;
        System.out.println(msg+"3");
        return session.send(msg);
        //return null;
    }
    */

    @OnClose
    public Publisher<String> onClose(
            String username,

            WebSocketSession session) {
        String msg = "[" + username + "] Disconnected!";
        return session.send(msg);
    }

    /*private Predicate<WebSocketSession> isValid(String topic) {
        return s -> topic.equalsIgnoreCase(s.getUriVariables().get("username", String.class, null));
    }
    */
}
