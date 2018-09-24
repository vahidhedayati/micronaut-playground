package beer.websocket;

import io.micronaut.websocket.WebSocketSession;

public class BeerSocketHandler {


    public BeerSocketHandler(WebSocketSession session,Long timeStamp) {
        this.timeStamp = timeStamp;
        this.session = session;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    Long timeStamp;


    public BeerSocketHandler() {

    }

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    WebSocketSession session;

}
