package micronaut.demo.beer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import io.micronaut.http.MediaType;
import io.micronaut.websocket.annotation.OnClose;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.micronaut.websocket.WebSocketSession;
import micronaut.demo.beer.model.BeerItem;
import micronaut.demo.beer.model.Ticket;
import org.reactivestreams.Publisher;
import java.util.function.Predicate;

@ServerWebSocket("/ws/{username}/{beerName}/{size}")
public class TransactionWebSocket {
    //String ticket,
    @OnOpen
    public Publisher<String> onOpen(String username, String beerName, String size, WebSocketSession session) {
        String msg = "" + username + ":"+beerName+':'+size;
        //BeerItem.Size.valueOf(size)
        System.out.print("________________________"+msg);

        return session.send("{ username:"+username+", beerName:"+beerName+", size: "+size+"}", MediaType.APPLICATION_JSON_TYPE);
    }

    @OnMessage
    public Publisher<String> onMessage(
            JSONPObject username,
            WebSocketSession session
    ) {
       //JsonParser jsonParser = new JsonFactory().createJsonParser(username);



        String msg = "[" + username.getClass() + "] ";//+beerName;// + beerName+' '+size;
        System.out.println(msg+"1");
    //    return session.send(msg);
     return null;
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

    private Predicate<WebSocketSession> isValid(String topic) {
        return s -> topic.equalsIgnoreCase(s.getUriVariables().get("username", String.class, null));
    }
}
