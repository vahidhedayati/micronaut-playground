package beer.websocket;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.websocket.WebSocketSession;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handshaker;
    final EmbeddedServer embeddedServer;

    private ChannelPromise handshakeFuture;
    private static final ChannelGroup channels = new DefaultChannelGroup(
            GlobalEventExecutor.INSTANCE);

    /*
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker , BillService billService) {
        this.handshaker = handshaker;
        this.billService=billService;
    }
    */

    @Inject
    public WebSocketClientHandler(final WebSocketClientHandshaker handshaker,EmbeddedServer embeddedServer) {
        this.handshaker = handshaker;
        this.embeddedServer=embeddedServer;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        //System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent
                .HANDSHAKE_COMPLETE) {
            ctx.channel().writeAndFlush(evt);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        //System.out.println(" ,sg "+msg);
        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        if (msg instanceof byte[]) {
            String msg1= new String((byte[])msg);
            System.out.println("Websocket Content "+msg1);//+
            if (msg1 == "__PING__") {
                System.out.println("Got a ping sending a pong back");
                ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));

            }
        }

        if (msg instanceof String) {
            System.out.println("Websocket Content String string "+msg);//+
            if (msg == "__PING__") {
                System.out.println("Got a ping sending a pong back");
                ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));
            }
        }

        if (msg instanceof PingWebSocketFrame) {
            System.out.println("WebSocket Client received ping --- sending pong back 3333 ");
            //ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));
        }

        if (msg instanceof ArrayList<?>) {
            //System.out.println("yes array list ");
            //if(((ArrayList<?>)msg).get(0) instanceof WebSocketSession) {
                System.out.println("Websocket sessions sent over ---");
                for (Object item : (ArrayList<?>) msg) {
                    //System.out.println("Trying to add  socket");//+((WebSocketSession)item).getId());
                    //TransactionWebSocket.addSession(((WebSocketSession) item));
                    WebSocketSession session = (WebSocketSession) item;
                    String hostPort = TransactionWebSocket.getKeyFromValue(TransactionWebSocket.liveConnections,session);
                    if (hostPort==null) {
                        System.out.println("WE NEED TO CONNECT");
                        session.sendAsync("CONNECT>"+embeddedServer.getHost()+":"+embeddedServer.getPort());
                    }

                }
                //System.out.println("Received all websocket sessions from another beersocket application");
            //}

        } else {
            System.out.println("ALL OTHER MESSAGE TYPE>>>" +msg.getClass());
            WebSocketFrame frame = (WebSocketFrame) msg;
            if (frame instanceof TextWebSocketFrame) {
                TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
                //System.out.println("WebSocket Client received message: " + textFrame.text()+"\n\n\n\n\n\n\n\n\n\n");
                //ctx.channel().writeAndFlush();
                String content = textFrame.text();

                System.out.println("Websocket Content "+content);//+

                if (content.contains("BROADCAST_SESSIONS")) {
                    System.out.println("BROADCAST_SESSIONS event happened");
                    //We send all the sessions of each server back to each other then we collect a few lines above
                    // around this bit msg instanceof ArrayList<?>
                    addSessions(TransactionWebSocket.getLiveSessions());
                }
                /*else if (content.contains("__PING__")) {
                    System.out.println("Got a ping sending a pong back");
                    ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));
                }
                */
            } else if (frame instanceof PingWebSocketFrame) {
                System.out.println("WebSocket Client received ping --- sending pong back aaaaaaaaaaaaaa");
                //  ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) frame).content()));
                ch.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content()));
            } else if (frame instanceof PongWebSocketFrame) {
                System.out.println("WebSocket Client received pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                System.out.println("WebSocket Client received closing");
                ch.close();
            }
        }



    }

    public  void addSessions(ArrayList<WebSocketSession> sessions) {
        System.out.println("adding All Sessions");
        /**
         * This at the moment is using a single consul host running locally
         * in wider world would point to a consul cluster
         * // compile "com.ecwid.consul:consul-api:1.4.1"
         *
         * sorry unsure perhaps micronaut has better ways seen classes covering consul discovery - wasn't sure how
         */
        ConsulClient client1 = new ConsulClient("localhost");

        /**
         * We ask consulClient to list all the healthynodes of beersocket applications running;
         */
        Response<List<HealthService>> healthyServices = client1.getHealthServices("beersocket", true, QueryParams.DEFAULT);
        List<HealthService> healthServices = healthyServices.getValue();
        healthServices.stream()
                .forEach(healthService -> {
                    HealthService.Service service =healthService.getService();
                    if (embeddedServer.getPort()!=service.getPort()) {
                        System.out.println("About to connect to websocket server for beersocket running on " + service.getAddress() + "/" + service.getPort());
                        final String url = "ws://" + service.getAddress() + ":" + service.getPort() + "/ls/SEND_" + embeddedServer.getPort();
                        try {
                            WebSocketClient client = new WebSocketClient(url);
                            client.open();
                            client.sendSessions(sessions);
                            client.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }

        ctx.close();
    }
}
