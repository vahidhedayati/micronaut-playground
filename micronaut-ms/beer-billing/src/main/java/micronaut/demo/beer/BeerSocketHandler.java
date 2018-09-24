package micronaut.demo.beer;

public class BeerSocketHandler {

    public WebSocketClient getClient() {
        return client;
    }

    public void setClient(WebSocketClient client) {
        this.client = client;
    }

    WebSocketClient client;

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    Long timeStamp;


    public BeerSocketHandler() {

    }

    public BeerSocketHandler(WebSocketClient client, Long timeStamp) {
            this.client=client;
            this.timeStamp=timeStamp;
    }
}
